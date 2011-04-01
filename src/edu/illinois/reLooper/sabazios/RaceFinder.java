package edu.illinois.reLooper.sabazios;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.classLoader.IMethod;
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

		Collection<CGNode> entrypointNodes = analysis.callGraph.getEntrypointNodes();

		for (CGNode cgNode : entrypointNodes) {
			explore(cgNode);
		}
		return races;
	}

	HashSet<CGNode> alreadyAnalyzed = new HashSet<CGNode>();
	private HashSet<Race> races = new HashSet<Race>();

	private void explore(CGNode node) {
		if (alreadyAnalyzed.contains(node) || isThreadSafe(node)) {
			return;
		}
		alreadyAnalyzed.add(node);

		if (node.getContext() instanceof FlexibleContext) {
			FlexibleContext c = (FlexibleContext) node.getContext();

			if (c.getItem(ArrayContextSelector.PARALLEL) != null
					&& ((Boolean) c.getItem(ArrayContextSelector.PARALLEL))
					&& ((Boolean) c.getItem(ArrayContextSelector.MAIN_ITERATION)) && node.getIR() != null) {

				IR ir = node.getIR();
				for (SSAInstruction instruction : ir.getControlFlowGraph().getInstructions()) {
					Race race = getRace(node, instruction);
					if (race != null)
						races.add(race);
				}
			}
		}

		Iterator<CGNode> succNodeCount = analysis.callGraph.getSuccNodes(node);
		while (succNodeCount.hasNext()) {
			CGNode succNode = (CGNode) succNodeCount.next();
			explore(succNode);
		}
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
				if (sharedObject != null && !isThreadSafe(sharedObject))
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
			if (sharedObject != null && !isThreadSafe(sharedObject))
				return new RaceOnNonStatic(new NormalStatement(node,
						CodeLocation.getSSAInstructionNo(node, instruction)), sharedObject);
		}
		return null;
	}

	
	String[] threadSafeMethods = new String[] {
			"java/util/regex/Pattern",
			"java/lang/System, exit",
			"java/io/PrintStream, print",
			"java/lang/Throwable, printStackTrace",
			"java/security/AccessControlContext, getDebug",
			"java.io.PrintStream, format",
			"java/util/Random, <init>"
	};
	
	private boolean isThreadSafe(CGNode node) {
		for (String pattern : threadSafeMethods)
			if (node.getMethod().toString().contains(pattern))
				return true;
		return false;
	}
	String[] threadSafeObjects = new String[] {
			"< Primordial, Ljava/lang/System, initializeSystemClass()V >:NEW <Primordial,Ljava/io/PrintStream>",
	};
	private boolean isThreadSafe(InstanceKey sharedObject) {
		for (String pattern : threadSafeObjects)
			if (sharedObject.toString().contains(pattern))
				return true;
		return false;
	}

}
