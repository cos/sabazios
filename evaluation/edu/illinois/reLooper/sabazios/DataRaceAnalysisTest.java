package edu.illinois.reLooper.sabazios;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.junit.Before;
import org.junit.Test;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
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

public abstract class DataRaceAnalysisTest {

	// what files/classes to include?
	// what are the entrypoints?

	protected Entrypoint entrypoint;
	protected PointerAnalysis pointerAnalysis;
	protected CallGraph callGraph;
	protected String entryClass;
	protected String entryMethod;
	protected final List<String> binaryDependencies = new ArrayList<String>();
	protected final List<String> sourceDependencies = new ArrayList<String>();
	protected final List<String> jarDependencies = new ArrayList<String>();
	protected PropagationCallGraphBuilder builder;
	
	public Set<Race> findRaces(String entryClass, String entryMethod)
	{
		try {
			setup(entryClass, entryMethod);
		
		BeforeInAfterVisitor beforeInAfter = new BeforeInAfterVisitor();
		ProgramTraverser programTraverser = new ProgramTraverser(callGraph,
				callGraph.getFakeRootNode(), beforeInAfter);
		programTraverser.traverse();

		Analysis analysis = new Analysis(callGraph, pointerAnalysis, builder,
				beforeInAfter);
		RaceFinder raceFinder = new RaceFinder(analysis);
		
		return raceFinder.findRaces();
		} catch (ClassHierarchyException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (CancelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addBinaryDependency(String path) {
		this.binaryDependencies.add(path);
	}

	public void addJarDependency(String file) {
		this.jarDependencies.add(file);
	}

	public void setup(String entryClass, String entryMethod) throws ClassHierarchyException,
			IllegalArgumentException, CancelException, IOException {
		this.entryClass = entryClass;
		this.entryMethod = entryMethod;
		AnalysisScope scope = getAnalysisScope();
		scope.setExclusions(FileOfClasses.createFileOfClasses(new File(
				"walaExclusions.txt")));

		IClassHierarchy cha = ClassHierarchy.make(scope);
//		System.out.println(scope);

		Set<Entrypoint> entrypoints = new HashSet<Entrypoint>();
		TypeReference typeReference = TypeReference.findOrCreate(
				scope.getLoader(AnalysisScope.APPLICATION),
				TypeName.string2TypeName(entryClass));
		MethodReference methodReference = MethodReference.findOrCreate(
				typeReference,
				entryMethod.substring(0, entryMethod.indexOf('(')),
				entryMethod.substring(entryMethod.indexOf('(')));

		entrypoint = new DefaultEntrypoint(methodReference, cha);
		entrypoints.add(entrypoint);

		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		AnalysisCache cache = new AnalysisCache();
		builder = makeZeroOneCFABuilder(
				options, cache, cha, scope);

		callGraph = builder.makeCallGraph(options);
		pointerAnalysis = builder.getPointerAnalysis();
	}

	private AnalysisScope getAnalysisScope() throws IOException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
		ClassLoaderReference loaderReference = scope
				.getLoader(AnalysisScope.APPLICATION);
		ClassLoader loader = DataRaceAnalysisTest.class.getClassLoader();

		// Add the the j2se jar files
		String[] stdlibs = WalaProperties.getJ2SEJarFiles();
		for (int i = 0; i < stdlibs.length; i++) {
			scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL),
					new JarFile(stdlibs[i]));
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

		// System.out.println(scope);
		return scope;
	}
	
	  public static SSAPropagationCallGraphBuilder makeZeroOneCFABuilder(AnalysisOptions options, AnalysisCache cache,
		      IClassHierarchy cha, AnalysisScope scope) {

		    if (options == null) {
		      throw new IllegalArgumentException("options is null");
		    }
		    Util.addDefaultSelectors(options, cha);
		    Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);
		    
//		    ContextSelector appContextSelector = new ContextInsensitiveSelector();
//			return new nCFABuilder(0, cha, options, cache, appContextSelector, new DefaultSSAInterpreter(options, cache));

		    return ZeroXCFABuilder.make(cha, options, cache, null, null, ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.SMUSH_MANY | ZeroXInstanceKeys.SMUSH_PRIMITIVE_HOLDERS
		        | ZeroXInstanceKeys.SMUSH_STRINGS | ZeroXInstanceKeys.SMUSH_THROWABLES);
		  }
}
