package racefix;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.util.U;
import sabazios.util.wala.viz.HeapGraphNodeDecorator;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.GraphUtil;

public class JMolEvaluationWithRaceDetection extends DataRaceAnalysisTest {

	public JMolEvaluationWithRaceDetection() {
		super();
		this.addBinaryDependency("../evaluation/jmol/bin");
	    this.addBinaryDependency("../lib/parallelArray.mock");
	    this.addJarFolderDependency("../evaluation/jmol/lib");
		U.detailedResults = false;
		this.projectName = "JMol";
	}

	@Test
	public void test() throws CancelException {
		findCA("Lorg/openscience/jmol/app/Jmol", MAIN_METHOD);
		
		final Privatizer privatizer = new Privatizer(a, a.deepRaces.values().iterator().next());
		privatizer.compute();
		
		HeapGraph heapGraph = a.heapGraph;
		@SuppressWarnings("deprecation")
		Graph<Object> prunedGraph = GraphSlicer.prune(heapGraph, new Filter<Object>() {
			@Override
			public boolean accepts(Object o) {
				return privatizer.getInstancesToPrivatize().contains(o) || privatizer.getFieldNodesToPrivatize().contains(o);
			}
		});
		a.dotGraph(prunedGraph, name.getMethodName(), new HeapGraphNodeDecorator(
				heapGraph) {
			@Override
			public String getDecoration(Object obj) {
				if(privatizer.getFieldNodesToPrivatize().contains(obj) )
					if(privatizer.shouldBeThreadLocal(((InstanceFieldKey) obj).getField()))
						return super.getDecoration(obj) +", style=filled, fillcolor=red";
					else
						return super.getDecoration(obj) +", style=filled, fillcolor=darkseagreen1";
				if(privatizer.getInstancesToPrivatize().contains(obj))
					return super.getDecoration(obj) +", style=filled, fillcolor=darkseagreen1";
				return super.getDecoration(obj);
			}
		});
	}
}