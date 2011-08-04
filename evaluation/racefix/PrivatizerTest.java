package racefix;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import racefix.refactoring.RefactoringElement;
import racefix.util.PrintUtil;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.Loop;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.wala.viz.HeapGraphNodeDecorator;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

import edu.uiuc.threadprivaterefactoring.ThreadPrivateRefactoring;

public class PrivatizerTest extends DataRaceAnalysisTest {

	@Rule
	public TestName name = new TestName();
	private Privatizer privatizer;
	private String className = null;;
	private String methodName = null;

	private IPath cuPath;

	public PrivatizerTest() {
		this.addBinaryDependency("bin/racefix");
		this.addBinaryDependency("bin/dummies");
		this.addBinaryDependency("../lib/parallelArray.mock");
	}

	@Before
	public void beforeTest() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
		setupAnalysis();
		setupWorkspace();
		privatizer.compute();
	}

	private void setupWorkspace() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Dummy");
		try {
			project.create(null);
			project.open(null);

			IProjectDescription description;
			description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);

			IJavaProject javaProject = JavaCore.create(project);

			IFolder binFolder = project.getFolder("bin"); // it does not need
																										// creating...
			javaProject.setOutputLocation(binFolder.getFullPath(), null);

			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
			for (LibraryLocation element : locations) {
				entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
			}

			IFolder sourceFolder = project.getFolder("src");
			sourceFolder.create(false, true, null);
			IFolder packageRootFolder = sourceFolder.getFolder("dummies");
			packageRootFolder.create(false, true, null);

			IPackageFragmentRoot rootPackage = javaProject.getPackageFragmentRoot(packageRootFolder);

			IPackageFragment pack = rootPackage.createPackageFragment("", true, null);

			String filePath = "dummies/dummies/" + name.getMethodName() + ".java";
			String fileData = getFileContents(filePath);

			ICompilationUnit cu = pack.createCompilationUnit(name.getMethodName() + ".java", fileData, true, null);
			IType[] allTypes = null;

			cuPath = cu.getPath();

			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = JavaCore.newSourceEntry(sourceFolder.getFullPath());

			project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		} catch (Throwable e) {
		}
	}

	private String getFileContents(String filePath) throws FileNotFoundException, IOException {
		File f = new File(filePath);
		return getFileContents(f);
	}

	private String getFileContents(File f) throws FileNotFoundException, IOException {
		String fileData = "";
		BufferedReader reader = new BufferedReader(new FileReader(f));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData += readData;
			buf = new char[1024];
		}
		reader.close();
		return fileData;
	}

	private void setupAnalysis() {
		if (className == null)
			className = "Lracefix/PrivatizerSubject";
		if (methodName == null)
			methodName = name.getMethodName();

		foundCA = findCA(className, methodName + "()V");
		Set<Loop> keySet = foundCA.keySet();
		Loop outer = null;
		for (Loop loop : keySet) {
			outer = loop;
		}

		Set<ConcurrentFieldAccess> accesses = (Set<ConcurrentFieldAccess>) foundCA.get(outer);

		privatizer = new Privatizer(a, accesses);
	}

	@After
	public void afterTest() throws Exception {
		HeapGraph heapGraph = a.heapGraph;
		a.dotGraph(heapGraph, name.getMethodName(), new HeapGraphNodeDecorator(heapGraph) {
			@Override
			public String getDecoration(Object obj) {
				if (privatizer.getFieldNodesToPrivatize().contains(obj))
					if (privatizer.shouldBeThreadLocal(((InstanceFieldKey) obj).getField()))
						return super.getDecoration(obj) + ", style=filled, fillcolor=red";
					else
						return super.getDecoration(obj) + ", style=filled, fillcolor=darkseagreen1";
				if (privatizer.getInstancesToPrivatize().contains(obj))
					return super.getDecoration(obj) + ", style=filled, fillcolor=darkseagreen1";
				return super.getDecoration(obj);
			}
		});

		destroyWorkspace();
	}

	private void destroyWorkspace() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Dummy");
		try {
			project.delete(true, null);
		} catch (CoreException e) {
		}
	}

	@Test
	public void simpleRace() throws Exception {
		className = null;
		methodName = null;
		String expectedLCD = "";
		String expectedNoLCD = "Object : racefix.PrivatizerSubject.simpleRace(PrivatizerSubject.java:83) new PrivatizerSubject$Particle\n" + 
				"   Alpha accesses:\n" + 
				"     Write racefix.PrivatizerSubject$1.op(PrivatizerSubject.java:88) - .coordX\n" + 
				"   Beta accesses:\n" + 
				"     Write racefix.PrivatizerSubject$1.op(PrivatizerSubject.java:88) - .coordX\n";
		assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
		assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
	}

	@Test
	public void writeReadRace() throws Exception {
		className = null;
		methodName = null;
		String expectedLCD = "";
		String expectedNoLCD = "Object : racefix.PrivatizerSubject.writeReadRace(PrivatizerSubject.java:97) new PrivatizerSubject$Particle\n" + 
				"   Alpha accesses:\n" + 
				"     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:102) - .coordX\n" + 
				"   Beta accesses:\n" + 
				"     Read racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:103) - .coordX\n" + 
				"     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:102) - .coordX\n";
		assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
		assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
	}

	@Test
	public void readWriteRace() throws Exception {
		className = null;
		methodName = null;
		String expectedLCD = "Object : racefix.PrivatizerSubject.readWriteRace(PrivatizerSubject.java:112) new PrivatizerSubject$Particle\n" + 
				"   Alpha accesses:\n" + 
				"     Write racefix.PrivatizerSubject$3.op(PrivatizerSubject.java:118) - .coordX\n" + 
				"   Beta accesses:\n" + 
				"     Write racefix.PrivatizerSubject$3.op(PrivatizerSubject.java:118) - .coordX\n" + 
				"     Read racefix.PrivatizerSubject$3.op(PrivatizerSubject.java:117) - .coordX\n";
		String expectedNoLCD = "";
		assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
		assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
	}

	@Test
	public void fieldSuperstar() throws Exception {
		className = null;
		methodName = null;
		assertEquals(
				"[< Application, Lracefix/PrivatizerSubject$Particle, next, <Application,Lracefix/PrivatizerSubject$Particle> >]",
				privatizer.getStarredFields());
	}

	@Test
	public void threadLocalOfClassWithComputationTest() throws Exception {
		// TODO
	}

	@Test
	public void testWorkspaceCorrectness() throws Exception {
		className = "Ldummies/testSimpleRefactoring";
		methodName = "simpleRace";
		setupWorkspace();

		IField findField = RefactoringElement.findField("src.dummies.testWorkspaceCorrectness.shared");
		assertNotNull(findField);
		destroyWorkspace();
	}

	@Test
	public void testSimpleRefactoring() throws FileNotFoundException, JavaModelException, IOException {
		className = "Ldummies/testSimpleRefactoring";
		methodName = "simpleRace";

		privatizer.compute();
		privatizer.refactor();
		assertFinalAs("testSimpleRefactoring_final.java");
		destroyWorkspace();
	}

	@Test
	public void testVerySimpleRefactoring() throws Exception {
		setupWorkspace();
		RefactoringElement element = new RefactoringElement("src.dummies.testVerySimpleRefactoring.shared",
				new ThreadPrivateRefactoring(RefactoringElement.findField("src.dummies.testVerySimpleRefactoring.shared")));
		element.apply();
		assertFinalAs("testVerySimpleRefactoring_final.java");
	}

	protected void assertFinalAs(String expectedFile) throws FileNotFoundException, IOException, JavaModelException {
		String expected = getFileContents("dummies/dummies/" + expectedFile);
		String filePath = cuPath.toOSString();
		String actual = getFileContents("../../junit-workspace/" + filePath);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void falseLCDInRenderMethodDespiteClearWriteBeforeRead() throws Exception {
		className = null;
		methodName = null;
		// String expectedLCD = "";
		// String expectedNoLCD = "";
		PrintUtil.writeLCDs(privatizer.getAccessesInLCDTestString(), name.getMethodName() + "LCDs.txt");
		PrintUtil.writeRacesToFile(a.deepRaces.values(), name.getMethodName() + "Races.txt");
		// assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
		// assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
	}
}