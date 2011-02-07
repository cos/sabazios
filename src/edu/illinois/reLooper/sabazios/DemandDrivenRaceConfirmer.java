package edu.illinois.reLooper.sabazios;

import java.util.Collection;

import com.ibm.wala.demandpa.alg.CallStack;
import com.ibm.wala.demandpa.alg.ContextSensitiveStateMachine;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo;
import com.ibm.wala.demandpa.alg.InstanceKeyAndState;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo.PointsToResult;
import com.ibm.wala.demandpa.alg.refinepolicy.TunedRefinementPolicy;
import com.ibm.wala.demandpa.alg.statemachine.StateMachine.State;
import com.ibm.wala.demandpa.flowgraph.DemandPointerFlowGraph;
import com.ibm.wala.demandpa.util.MemoryAccessMap;
import com.ibm.wala.demandpa.util.PABasedMemoryAccessMap;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallerSiteContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ipa.slicer.Statement.Kind;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.Pair;

public class DemandDrivenRaceConfirmer {
	private final Analysis analysis;

	public DemandDrivenRaceConfirmer(Analysis analysis) {
		this.analysis = analysis;
	}

	public boolean confirm(LocalPointerKey localPointerKey,
			InOutVisitor beforeInAfter) {
		CallGraph cg = analysis.callGraph;
		MemoryAccessMap mam = new PABasedMemoryAccessMap(cg,
				analysis.pointerAnalysis);
		IClassHierarchy cha = analysis.pointerAnalysis.getClassHierarchy();
		HeapModel model = analysis.pointerAnalysis.getHeapModel();
		AnalysisOptions options = analysis.builder.getOptions();
		ContextSensitiveStateMachine.Factory stateMachineFactory = new ContextSensitiveStateMachine.Factory();

		DemandRefinementPointsTo demandRefinementPointsTo = DemandRefinementPointsTo
				.makeWithDefaultFlowGraph(cg, model, mam, cha, options,
						stateMachineFactory);
		
		demandRefinementPointsTo.setRefinementPolicyFactory(new MyRefinementPolicy.Factory());

		Pair<PointsToResult, Collection<InstanceKeyAndState>> result = demandRefinementPointsTo
				.getPointsToWithStates(localPointerKey,
						Predicate.<InstanceKey> falsePred());

		Collection<InstanceKeyAndState> pointsTo = result.snd;
		
		if(pointsTo == null)
			return true;

		for (InstanceKeyAndState instanceKey : pointsTo) {
			CallStack state = (CallStack) instanceKey.getState();
			for (CallerSiteContext callerSiteContext : state) {
				NormalStatement s = new NormalStatement(callerSiteContext.getCaller(), callerSiteContext.getCallSite().getProgramCounter());
				if(beforeInAfter.in.contains(s) && !beforeInAfter.out.contains(s))
					return false;
			}
		}
		return true;
	}
}
