package edu.illinois.reLooper.sabazios;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.demandpa.alg.CallStack;
import com.ibm.wala.demandpa.alg.ContextSensitiveStateMachine;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo.PointsToResult;
import com.ibm.wala.demandpa.alg.InstanceKeyAndState;
import com.ibm.wala.demandpa.alg.refinepolicy.CallGraphRefinePolicy;
import com.ibm.wala.demandpa.alg.refinepolicy.FieldRefinePolicy;
import com.ibm.wala.demandpa.alg.refinepolicy.TunedRefinementPolicy;
import com.ibm.wala.demandpa.alg.statemachine.StateMachine.State;
import com.ibm.wala.demandpa.flowgraph.IFlowLabel;
import com.ibm.wala.demandpa.util.MemoryAccessMap;
import com.ibm.wala.demandpa.util.PABasedMemoryAccessMap;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallerSiteContext;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.Pair;

public class DemandDrivenTest extends DataRaceAnalysisTest {

	private final class MyPredicate extends Predicate<InstanceKey> {
		@Override
		public boolean test(InstanceKey t) {
			return false;
		}
	}

	public DemandDrivenTest() {
		this.setBinaryDependency("subjects");
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
					.makeWithDefaultFlowGraph(callGraph,
							pointerAnalysis.getHeapModel(), mam, cha, options,
							stateMachineFactory);

			demandRefinementPointsTo
					.setRefinementPolicyFactory(new MyRefinementPolicy.Factory());

			// executeFor(demandRefinementPointsTo, "main", 4);
			executeFor(demandRefinementPointsTo, "main", "y");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeFor(DemandRefinementPointsTo demandRefinementPointsTo,
			String nodeHint, CharSequence value) {
		LocalPointerKey localPointerKey = getInterestingPointerKey(nodeHint,
				value);

		System.out.println("Start");
		Pair<PointsToResult, Collection<InstanceKeyAndState>> result = demandRefinementPointsTo
				.getPointsToWithStates(localPointerKey, new MyPredicate());
		System.out.println(result.fst);
		Collection<InstanceKeyAndState> pointsTo = result.snd;
		for (InstanceKeyAndState instanceKey : pointsTo) {
			CallStack callStack = (CallStack) instanceKey.getState();
			System.out.println(instanceKey);
		}
	}

	private LocalPointerKey getInterestingPointerKey(CharSequence nodeHint,
			CharSequence variableHint) {
		LocalPointerKey localPointerKey;

		Collection<PointerKey> pointerKeys = pointerAnalysis.getPointerKeys();

		for (PointerKey pointerKey : pointerKeys) {
			if (pointerKey instanceof LocalPointerKey) {
				localPointerKey = (LocalPointerKey) pointerKey;
				String variableName = CodeLocation.variableName(
						localPointerKey.getValueNumber(),
						localPointerKey.getNode(), localPointerKey.getNode()
								.getIR().getInstructions().length - 1);

				if (localPointerKey.getNode().toString().contains(nodeHint)
						&& variableName != null && variableName.equals(variableHint))
					return localPointerKey;
			}
		}
		return null;
	}
}