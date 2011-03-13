package edu.illinois.reLooper.sabazios;

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

	private final boolean DEBUG = false;
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

	public Set<Race> findRaces() throws CancelException {
		HashSet<Race> races = new HashSet<Race>();

		// for each statement in the parallel for
		for (CGNode node : callGraph)
			if (node.getContext() instanceof SmartContextSelector.InsideParOpContext)
				for (SSAInstruction instruction : node.getIR().getControlFlowGraph().getInstructions()) {

					// we only check put instructions for now. should also
					// include
					// array, etc. in the future
					if (instruction instanceof SSAPutInstruction) {
						SSAPutInstruction putI = (SSAPutInstruction) instruction;

						if (DEBUG)
							System.out.println(instruction);

						// if it is not static, we do the more involved check
						if (!putI.isStatic()) {
							int ref = putI.getRef();

							Set<AllocationSiteInNode> allocSites = Analysis.getOutsideAllocationSites(
									node, ref);

							// do a demand driven confirmation of the race
							// if (allocSites ){
							// DemandDrivenRaceConfirmer raceConfirmer = new
							// DemandDrivenRaceConfirmer(
							// analysis);
							// racing = raceConfirmer.confirm(localPointerKey,
							// beforeInAfter);
							// }

							// report race if it still stands
							if (!allocSites.isEmpty()) {
								Race race = new Race(new NormalStatement(node, CodeLocation.getSSAInstructionNo(node, instruction)), allocSites.iterator().next(), false);
								races.add(race);
							}

						} else {
							// TODO: replace false with the true value of
							// isLoopCarriedDependency
							Race race = new Race(new NormalStatement(node, CodeLocation.getSSAInstructionNo(node, instruction)), null, false);
							races.add(race);
							if (DEBUG)
								System.out.println(race);
						}
					}
				}
		return races;
	}

	private boolean checkLoopCarryDependency(final StatementWithInstructionIndex statement,
			final AllocationSiteInNode instanceKey, final InOutVisitor beforeInAfter) throws CancelException {
		// Collection<Statement> backwordSlice = Slicer.computeBackwardSlice(
		// statement, callGraph, pointerAnalysis,
		// DataDependenceOptions.FULL,
		// ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);

		PredUseVisitor predUseVisitor = new PredUseVisitor(analysis, beforeInAfter, instanceKey, statement);

		(new ProgramTraverser(callGraph, callGraph.getFakeRootNode(), predUseVisitor)).traverse();

		return predUseVisitor.foundUse;
	}
}
