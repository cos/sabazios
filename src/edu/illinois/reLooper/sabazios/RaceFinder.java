package edu.illinois.reLooper.sabazios;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.race.Race;
import edu.illinois.reLooper.sabazios.race.RaceOnNonStatic;
import edu.illinois.reLooper.sabazios.race.RaceOnStatic;

public class RaceFinder {

	private static final boolean DEBUG = false;
	private final Analysis analysis;

	public RaceFinder(Analysis analysis) {
		this.analysis = analysis;
	}

	public Set<Race> findRaces() throws CancelException {
		HashSet<Race> races = new HashSet<Race>();

		for (CGNode node : analysis.callGraph) {
			if (!(node.getContext() instanceof FlexibleContext))
				continue;

			FlexibleContext c = (FlexibleContext) node.getContext();

			if (c.getItem(ArrayContextSelector.PARALLEL) != null
					&& ((Boolean) c.getItem(ArrayContextSelector.PARALLEL))
					&& ((Boolean) c.getItem(ArrayContextSelector.MAIN_ITERATION))) {
				IR ir = node.getIR();
				if (ir == null)
					continue;
				for (SSAInstruction instruction : ir.getControlFlowGraph().getInstructions()) {
					Race race = getRace(node, instruction);
					if (race != null)
						races.add(race);
				}
			}
		}
		return races;
	}

	private Race getRace(CGNode node, SSAInstruction instruction) {
		// TODO: we only check put instructions for now. should also include
		// array, etc. in the future
		if (instruction instanceof SSAPutInstruction) {
			SSAPutInstruction putI = (SSAPutInstruction) instruction;

			if (DEBUG)
				System.out.println(instruction);
			// if it is not static, we do the more involved check
			if (!putI.isStatic()) {
				int ref = putI.getRef();
				InstanceKey sharedObject = analysis.traceBackToShared(node, ref);

				// report race if it still stands
				if (!(sharedObject == null))
					return new RaceOnNonStatic(new NormalStatement(node, CodeLocation.getSSAInstructionNo(node,
							instruction)), sharedObject);

			} else {
				// TODO: replace false with the true value of
				// isLoopCarriedDependency
				return new RaceOnStatic(new NormalStatement(node, CodeLocation.getSSAInstructionNo(node, instruction)));
			}
		}

		if (instruction instanceof SSAArrayStoreInstruction) {
			SSAArrayStoreInstruction putI = (SSAArrayStoreInstruction) instruction;

			if (DEBUG)
				System.out.println(instruction);
			// if it is not static, we do the more involved check
			int ref = putI.getArrayRef();
			InstanceKey sharedObject = analysis.traceBackToShared(node, ref);

			// report race if it still stands
			if (!(sharedObject == null))
				return new RaceOnNonStatic(new NormalStatement(node,
						CodeLocation.getSSAInstructionNo(node, instruction)), sharedObject);
		}
		return null;
	}
}
