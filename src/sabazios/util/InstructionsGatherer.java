package sabazios.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.A;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;

public abstract class InstructionsGatherer {

	// hack to get the number of methods in the program.
	public static Set<IMethod> methods;

	public void compute(A a) {
		methods = new HashSet<IMethod>();
		Collection<CGNode> entrypointNodes = a.callGraph.getEntrypointNodes();
		for (CGNode cgNode : entrypointNodes) {
			explore(a , cgNode);
		}
	}

	HashSet<CGNode> alreadyAnalyzed = new HashSet<CGNode>();

	private void explore(A a, CGNode n) {
		if (alreadyAnalyzed.contains(n) || !shouldVisit(n)) {
			return;
		}
		
		// hack to get the number of distinct analyzed methods in the program
		if(n.getIR() != null) 
			InstructionsGatherer.methods.add(n.getIR().getMethod());
		
		alreadyAnalyzed.add(n);
		if (shouldAnalyze(n)) {
			IR ir = n.getIR();
			for (SSAInstruction i : ir.getControlFlowGraph().getInstructions()) {
				visit(n, i);
			}
		}

		Iterator<CGNode> succNodeCount = a.callGraph.getSuccNodes(n);
		while (succNodeCount.hasNext()) {
			CGNode succNode = succNodeCount.next();
			explore(a, succNode);
		}
	}

	protected abstract void visit(CGNode n, SSAInstruction i);

	protected abstract boolean shouldVisit(CGNode n);

	protected abstract boolean shouldAnalyze(CGNode n);
}
