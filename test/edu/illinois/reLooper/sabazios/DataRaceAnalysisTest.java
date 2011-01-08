package edu.illinois.reLooper.sabazios;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

import org.junit.Before;
import org.junit.Test;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
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
import com.ibm.wala.util.io.FileProvider;

public class DataRaceAnalysisTest {

	// what files/classes to include?
	// what are the entrypoints?

	protected Entrypoint entrypoint;
	protected PointerAnalysis pointerAnalysis;
	protected CallGraph callGraph;

	@Before
	public void setup() throws ClassHierarchyException,
			IllegalArgumentException, CancelException, IOException {
		AnalysisScope scope = getAnalysisScope();
		scope.setExclusions(FileOfClasses.createFileOfClasses(new File("walaExclusions.txt")));

		IClassHierarchy cha = ClassHierarchy.make(scope);
		
		
		Set<Entrypoint> entrypoints = new HashSet<Entrypoint>();
		
		String classString =  "Lsubjects/Particles";
		String methodString = "main()V";
		
		
		TypeReference typeReference = TypeReference.findOrCreate(
				scope.getLoader(AnalysisScope.APPLICATION),
				TypeName.string2TypeName(classString));
		MethodReference methodReference = MethodReference.findOrCreate(
				typeReference, methodString .substring(0, methodString.indexOf('(')), methodString
				.substring(methodString.indexOf('(')));
		
		entrypoint = new DefaultEntrypoint(methodReference, cha);
		entrypoints.add(entrypoint);
		
		
		
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		AnalysisCache cache = new AnalysisCache();
		PropagationCallGraphBuilder builder = Util.makeZeroOneCFABuilder(options, cache, cha, scope);

		callGraph = builder.makeCallGraph(options);
		pointerAnalysis = builder.getPointerAnalysis();
	}

	@Test
	public void bla() throws CancelException {
		BeforeInAfterVisitor beforeInAfter = new BeforeInAfterVisitor();
		ProgramTraverser programTraverser = new ProgramTraverser(callGraph,
				callGraph.getFakeRootNode(), beforeInAfter);
		programTraverser.traverse();

		Analysis analysis = new Analysis(callGraph, pointerAnalysis,
				beforeInAfter);
		RaceFinder raceFinder = new RaceFinder(analysis);
		
		Set<Race> races = raceFinder.findRaces(beforeInAfter);
		
		System.out.println(races);
	}

	private AnalysisScope getAnalysisScope() throws IOException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

		// Add the the j2se jar files
		String[] stdlibs = WalaProperties.getJ2SEJarFiles();
		for (int i = 0; i < stdlibs.length; i++) {
			scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL),
					new JarFile(stdlibs[i]));
		}
		ClassLoaderReference loader = scope
				.getLoader(AnalysisScope.APPLICATION);
		
		String[] directoriesToLoad = new String[] { "subjects", "jsr166y", "extra166y"};
		
		for (String directory : directoriesToLoad) {
			File sd = FileProvider.getFile(directory,
					DataRaceAnalysisTest.class.getClassLoader());
			assert sd.isDirectory();
			scope.addToScope(loader, new BinaryDirectoryTreeModule(sd));
		}

//		System.out.println(scope);
		return scope;
	}
}
