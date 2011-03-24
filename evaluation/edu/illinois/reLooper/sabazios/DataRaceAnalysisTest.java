package edu.illinois.reLooper.sabazios;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.junit.ComparisonFailure;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.io.FileProvider;

import edu.illinois.reLooper.sabazios.race.Race;
import edu.illinois.reLooper.sabazios.race.RaceOnNonStatic;
import extra166y.ParallelArray;

import static junit.framework.Assert.*;

public abstract class DataRaceAnalysisTest {

	// what files/classes to include?
	// what are the entrypoints?

	protected static String testClassName;
	protected Entrypoint entrypoint;
	protected PointerAnalysis pointerAnalysis;
	protected CallGraph callGraph;
	protected String entryClass;
	protected String entryMethod;
	protected final List<String> binaryDependencies = new ArrayList<String>();
	protected final List<String> sourceDependencies = new ArrayList<String>();
	protected final List<String> jarDependencies = new ArrayList<String>();
	protected PropagationCallGraphBuilder builder;
	protected static boolean DEBUG = false;
	protected Analysis analysis;
	protected Set<Race> foundRaces;

	public DataRaceAnalysisTest() {
		testClassName = this.getClass().getName();
	}

	public Set<Race> findRaces(String entryClass, String entryMethod) {
		
			try {
				setup(entryClass, entryMethod);
			} catch (ClassHierarchyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CancelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Initial setup");


			System.out.println("before in after done");

			analysis = new Analysis(callGraph, pointerAnalysis, builder);
			Analysis.instance = analysis;
			RaceFinder raceFinder = new RaceFinder(analysis);

			Set<Race> races = null;
			try {
				races = raceFinder.findRaces();
			} catch (CancelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return races;
		
	}

	public void setBinaryDependency(String path) {
		this.binaryDependencies.add(path);
	}

	public void addJarDependency(String file) {
		this.jarDependencies.add(file);
	}

	public void setup(String entryClass, String entryMethod) throws ClassHierarchyException, IllegalArgumentException,
			CancelException, IOException {
		this.entryClass = entryClass;
		this.entryMethod = entryMethod;
		AnalysisScope scope = getAnalysisScope();
		scope.setExclusions(FileOfClasses.createFileOfClasses(new File("walaExclusions.txt")));
		
//		System.out.println(scope);

		IClassHierarchy cha = ClassHierarchy.make(scope);

		Set<Entrypoint> entrypoints = new HashSet<Entrypoint>();
		TypeReference typeReference = TypeReference.findOrCreate(scope.getLoader(AnalysisScope.APPLICATION),
				TypeName.string2TypeName(entryClass));
		MethodReference methodReference = MethodReference.findOrCreate(typeReference,
				entryMethod.substring(0, entryMethod.indexOf('(')), entryMethod.substring(entryMethod.indexOf('(')));

		entrypoint = new DefaultEntrypoint(methodReference, cha);
		entrypoints.add(entrypoint);

		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		AnalysisCache cache = new AnalysisCache();
		builder = makeCFABuilder(options, cache, cha, scope);

		callGraph = builder.makeCallGraph(options);
		pointerAnalysis = builder.getPointerAnalysis();
	}

	private AnalysisScope getAnalysisScope() throws IOException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
		ClassLoaderReference loaderReference = scope.getLoader(AnalysisScope.APPLICATION);
		ClassLoader loader = DataRaceAnalysisTest.class.getClassLoader();

		// Add the the j2se jar files
		String[] stdlibs = WalaProperties.getJ2SEJarFiles();
		for (int i = 0; i < stdlibs.length; i++) {
			scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL), new JarFile(stdlibs[i]));
		}

		for (String directory : binaryDependencies) {
			File sd = FileProvider.getFile(directory, loader);
			assert sd.isDirectory();
			scope.addToScope(loaderReference, new BinaryDirectoryTreeModule(sd));
		}

		for (String path : jarDependencies) {
			Module M = FileProvider.getJarFileModule(path, loader);
			scope.addToScope(loaderReference, M);
		}

		return scope;
	}

	protected void printDetailedRaces(Set<Race> foundRaces) {
		System.out.println("Number of races: " + foundRaces.size());
		for (Iterator<Race> iterator = foundRaces.iterator(); iterator.hasNext();) {
			RaceOnNonStatic race = (RaceOnNonStatic) iterator.next();
			System.out.println(race.toDetailedString(callGraph));
		}
	}

	protected void printRaces(Set<RaceOnNonStatic> races) {
		System.out.println("Number of races: " + races.size());
		for (Iterator<RaceOnNonStatic> iterator = races.iterator(); iterator.hasNext();) {
			RaceOnNonStatic race = (RaceOnNonStatic) iterator.next();
			System.out.println(race);
		}
	}

	public void assertNoRaces() {
		assertRaces();
	}
	
	public void findRaces() {
		foundRaces = findRaces(getTestClassName(), getEntryMethod());
	}
	
	public void assertRaces(String... expected) {
		List<String> expectedRaces = Arrays.asList(expected);
		if (DEBUG) {
			System.err.println(getCurrentlyExecutingTestName());
			printDetailedRaces(foundRaces);
		}
		if (foundRaces.size() == 0 && expectedRaces == null)
			return;
		if (expectedRaces != null && expectedRaces.size() == foundRaces.size()) {
			boolean noMatch = false;
			for (Race r : foundRaces) {
				if (!expectedRaces.contains(CodeLocation.make(r.getStatement()).toString())) {
					System.out.println(CodeLocation.make(r.getStatement()).toString());
					noMatch = true;
					break;
				}
			}
			if(noMatch)
				fail(expectedRaces, foundRaces);
			else
				return;
		}

		fail(expectedRaces, foundRaces);
	}

	protected String getEntryMethod() {
		return getCurrentlyExecutingTestName() + "()V";
	}

	protected String getTestClassName() {
		String testClassName = this.getClass().getSimpleName();
		String className = testClassName.substring(0, testClassName.length() - 4);
		String classNameForWala = "Lsynthetic/" + className;
		return classNameForWala;
	}

	private void fail(Collection<String> expectedRaces, Set<Race> foundRaces) throws ComparisonFailure {
		String expected = "";
		if (expectedRaces != null)
			for (String s : expectedRaces)
				expected += s + "\n";

		System.err.println("Expected: \n" + expected + "Found:");
		String actual = "";
		for (Race r : foundRaces) {
			actual += CodeLocation.make(r.getStatement()) + "\n";
			System.err.println(r);
		}

		throw new ComparisonFailure("Different data races", expected, actual);
	}

	protected void assertRace(String location) {
		assertRaces(location);
	}

	public static SSAPropagationCallGraphBuilder makeCFABuilder(AnalysisOptions options, AnalysisCache cache,
			IClassHierarchy cha, AnalysisScope scope) {

		if (options == null) {
			throw new IllegalArgumentException("options is null");
		}
		Util.addDefaultSelectors(options, cha);
		Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);

		DefaultContextSelector appContextSelector = new DefaultContextSelector(options);
		// new VariableCFAContextSelector(new ContextInsensitiveSelector());
//		return new VariableCFABuilder(cha, options, cache, appContextSelector,
//				new DefaultSSAInterpreter(options, cache),
//				// ZeroXInstanceKeys.SMUSH_MANY |
//				ZeroXInstanceKeys.SMUSH_STRINGS |
//				// ZeroXInstanceKeys.SMUSH_THROWABLES |
//				ZeroXInstanceKeys.ALLOCATIONS);
		
		return new SmartCFABuilder(cha, options, cache, appContextSelector, new DefaultSSAInterpreter(options, cache), 
				// ZeroXInstanceKeys.SMUSH_MANY |
				ZeroXInstanceKeys.SMUSH_STRINGS |
				// ZeroXInstanceKeys.SMUSH_THROWABLES |
				ZeroXInstanceKeys.ALLOCATIONS);
	}

	protected String getCurrentlyExecutingTestName() {
		Throwable t = new Throwable();
		StackTraceElement[] elements = t.getStackTrace();
		if (elements.length <= 0)
			return "[No Stack Information Available]";
		// elements[0] is this method
		if (elements.length < 2)
			return null;

		for (StackTraceElement stackTraceElement : elements) {
			if (stackTraceElement.getClassName() == testClassName)
				return stackTraceElement.getMethodName();
		}
		return "[Not in a test hierarchy]";
	}
}
