package edu.illinois.reLooper.sabazios;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import com.ibm.wala.demandpa.alg.ContextSensitiveStateMachine;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo.PointsToResult;
import com.ibm.wala.demandpa.alg.refinepolicy.TunedRefinementPolicy;
import com.ibm.wala.demandpa.util.MemoryAccessMap;
import com.ibm.wala.demandpa.util.PABasedMemoryAccessMap;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.Pair;

public class DemandDrivenTest extends DataRaceAnalysisTest {

	public DemandDrivenTest() {
		this.addBinaryDependency("subjects");
	}

	@Test
	public void doTest() {
		try {
			setup("Lsubjects/DemandTest", "main()V");
			
			MemoryAccessMap mam = new PABasedMemoryAccessMap(callGraph,
					pointerAnalysis);
			IClassHierarchy cha = pointerAnalysis.getClassHierarchy();
			AnalysisOptions options = builder.getOptions();
			ContextSensitiveStateMachine.Factory stateMachineFactory = new ContextSensitiveStateMachine.Factory();

			DemandRefinementPointsTo demandRefinementPointsTo = DemandRefinementPointsTo
					.makeWithDefaultFlowGraph(callGraph, pointerAnalysis.getHeapModel(), mam, cha, options,
							stateMachineFactory);
			
			demandRefinementPointsTo.setRefinementPolicyFactory(new TunedRefinementPolicy.Factory(cha));

			LocalPointerKey localPointerKey = null;
			
			Pair<PointsToResult, Collection<InstanceKey>> result = demandRefinementPointsTo
					.getPointsTo(localPointerKey,
							Predicate.<InstanceKey> falsePred());

			System.out.println(result.fst);

			Collection<InstanceKey> pointsTo = result.snd;

			for (InstanceKey instanceKey : pointsTo) {
				System.out.println(instanceKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}