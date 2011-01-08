package edu.illinois.reLooper.suggest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.util.CancelException;

public class RaceFinder {

	private final PointerAnalysis pointerAnalysis;
	private final CallGraph callGraph;
	private HeapGraph heapGraph;
	private final Analysis analysis;

	public RaceFinder(Analysis analysis) {
		this.analysis = analysis;
		this.pointerAnalysis = analysis.pointerAnalysis;
		this.callGraph = analysis.callGraph;
		this.heapGraph = pointerAnalysis.getHeapGraph();
	}

	public Set<Race> findRaces(BeforeInAfterVisitor beforeInAfter)
			throws CancelException {

		HashSet<Race> races = new HashSet<Race>();

		for (StatementWithInstructionIndex statement : beforeInAfter.in) {
			CGNode cgNode = statement.getNode();
			SSAInstruction instruction = cgNode.getIR().getInstructions()[statement
					.getInstructionIndex()];
			if (instruction instanceof SSAPutInstruction) {
				SSAPutInstruction putI = (SSAPutInstruction) instruction;
				if (!putI.isStatic()) {
					int ref = putI.getRef();
					Iterator<Object> instanceKeys = analysis.getInstanceKeys(cgNode, ref);

					while (instanceKeys.hasNext()) {
						Object object = (Object) instanceKeys.next();
						if (object instanceof AllocationSiteInNode) {

							NormalStatement allocationSiteStatement = getNormalStatement((AllocationSiteInNode) object);

							if (beforeInAfter.before
									.contains(allocationSiteStatement)) {

								boolean loopCarryDependency = checkLoopCarryDependency(statement, (AllocationSiteInNode) object, beforeInAfter);

								Race race = new Race(
										(NormalStatement) statement,
										(AllocationSiteInNode) object, loopCarryDependency);
								races.add(race);
							}
						}
					}

				} else {
					System.out.println("HERE "+statement);
					// TODO: replace false with the true value of
					// isLoopCarriedDependency
					races.add(new Race((NormalStatement) statement, null, false));
				}
			}
		}
		return races;
	}

	private boolean checkLoopCarryDependency(
			final StatementWithInstructionIndex statement, final AllocationSiteInNode instanceKey, final BeforeInAfterVisitor beforeInAfter) throws CancelException {
//		Collection<Statement> backwordSlice = Slicer.computeBackwardSlice(
//				statement, callGraph, pointerAnalysis,
//				DataDependenceOptions.FULL,
//				ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);
		
		PredUseVisitor predUseVisitor = new PredUseVisitor(analysis, beforeInAfter, instanceKey, statement);

		(new ProgramTraverser(callGraph, callGraph.getFakeRootNode(),predUseVisitor)).traverse();
		
		return predUseVisitor.foundUse;
	}

	private NormalStatement getNormalStatement(AllocationSiteInNode instanceKey) {
		SSANewInstruction allocationSiteInstruction = getInstruction(instanceKey);
		int allocationSiteInstructionIndex = CodeLocation.getSSAInstructionNo(
				instanceKey.getNode(), allocationSiteInstruction);
		NormalStatement allocationSiteStatement = new NormalStatement(
				instanceKey.getNode(), allocationSiteInstructionIndex);
		return allocationSiteStatement;
	}

	public SSANewInstruction getInstruction(AllocationSiteInNode p) {
		if (p.getNode().getIR() != null)
			return p.getNode().getIR().getNew(p.getSite());
		else
			return null;
	}
}
