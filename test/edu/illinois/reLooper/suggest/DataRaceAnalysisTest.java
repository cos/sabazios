package edu.illinois.reLooper.suggest;

import java.io.IOException;
import java.util.jar.JarFile;

import org.junit.*;

import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysisImpl;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.PointsToMap;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.intset.MutableMapping;

public class DataRaceAnalysisTest {

	// what files/classes to include?
	// what are the entrypoints?

	private Iterable<? extends Entrypoint> entrypoints;

	@Before
	public void setup() throws ClassHierarchyException,
			IllegalArgumentException, CancelException, IOException {

		AnalysisScope scope = getAnalysisScope();

		IClassHierarchy cha = ClassHierarchy.make(scope);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		AnalysisCache cache = new AnalysisCache();
		SSAContextInterpreter appContextInterpreter;
		PropagationCallGraphBuilder builder = new nCFABuilder(0, cha, options,
				cache, null, null);
		
		
		CallGraph cg = builder.makeCallGraph(options);
		PointsToMap pointsToMap;
	}

	private AnalysisScope getAnalysisScope() throws IOException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

		// Add the the j2se jar files
		String[] stdlibs = WalaProperties.getJ2SEJarFiles();
		for (int i = 0; i < stdlibs.length; i++) {
			scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL),
					new JarFile(stdlibs[i]));
		}
		return scope;
	}
}
