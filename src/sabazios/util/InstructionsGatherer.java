package sabazios.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import sabazios.A;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;

public abstract class InstructionsGatherer {

	public void compute() {
		Collection<CGNode> entrypointNodes = A.callGraph.getEntrypointNodes();
		for (CGNode cgNode : entrypointNodes) {
			explore(cgNode);
		}
	}

	HashSet<CGNode> alreadyAnalyzed = new HashSet<CGNode>();

	private void explore(CGNode n) {
		if (alreadyAnalyzed.contains(n) || !shouldVisit(n)) {
			return;
		}
		alreadyAnalyzed.add(n);
		if (shouldAnalyze(n)) {
			IR ir = n.getIR();
			for (SSAInstruction i : ir.getControlFlowGraph().getInstructions()) {
				visit(n, i);
			}
		}

		Iterator<CGNode> succNodeCount = A.callGraph.getSuccNodes(n);
		while (succNodeCount.hasNext()) {
			CGNode succNode = succNodeCount.next();
			explore(succNode);
		}
	}

	protected abstract void visit(CGNode n, SSAInstruction i);

	protected abstract boolean shouldVisit(CGNode n);

	protected abstract boolean shouldAnalyze(CGNode n);
}
