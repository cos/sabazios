package edu.illinois.reLooper.sabazios;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.demandpa.alg.ContextSensitiveStateMachine;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo;
import com.ibm.wala.demandpa.util.MemoryAccessMap;
import com.ibm.wala.demandpa.util.PABasedMemoryAccessMap;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.graph.traverse.DFSPathFinder;
import com.ibm.wala.util.collections.Filter;

;

public class ManuThesisTest extends DataRaceAnalysisTest {

	public ManuThesisTest() {
		super();
		this.setBinaryDependency("subjects");
	}

	@Test
	public void main() throws CancelException {
		try {
			setup(getTestClassName(), getCurrentlyExecutingTestName() + "()V");

			// System.out.println(GraphPrint.genericToString(callGraph));
			System.out.println("----------");
			HeapGraph heapGraph = this.pointerAnalysis.getHeapGraph();
			Object pointer = getFirstMatch(heapGraph, "\\[Node.*mark.*");

			MemoryAccessMap mam = new PABasedMemoryAccessMap(callGraph, pointerAnalysis);
			IClassHierarchy cha = pointerAnalysis.getClassHierarchy();
			AnalysisOptions options = builder.getOptions();
			ContextSensitiveStateMachine.Factory stateMachineFactory = new ContextSensitiveStateMachine.Factory();
			DemandRefinementPointsTo demandRefinementPointsTo = DemandRefinementPointsTo.makeWithDefaultFlowGraph(
					callGraph, pointerAnalysis.getHeapModel(), mam, cha, options, stateMachineFactory);
			demandRefinementPointsTo.setRefinementPolicyFactory(new MyRefinementPolicy.Factory());
			
			Collection<InstanceKey> pointsTo = demandRefinementPointsTo.getPointsTo((PointerKey) pointer);
			printPointsToSet(pointsTo);

		} catch (ClassHierarchyException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (CancelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printPointsToSet(Collection<InstanceKey> pointsTo) {
		for (InstanceKey instanceKey : pointsTo) {
			System.out.println(instanceKey);
			AllocationSiteInNode as = (AllocationSiteInNode) instanceKey;
			CodeLocation location = CodeLocation.make(as.getNode(), as.getSite().getProgramCounter());
			System.out.println(location);
		}
	}

	private Object getFirstMatch(HeapGraph heapGraph, String string) {
		Iterator<Object> iterator = heapGraph.iterator();
		while (iterator.hasNext()) {
			Object object = (Object) iterator.next();
			if (object.toString().matches(string))
				return object;
		}
		return null;
	}
}
