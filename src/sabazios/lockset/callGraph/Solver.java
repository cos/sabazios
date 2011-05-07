package sabazios.lockset.callGraph;

import java.util.HashMap;

import sabazios.util.IntSetVariable;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.SSAInstruction;

public class Solver extends DataflowSolver<CGNode, Lock> {

	public static class Problem extends BasicFramework<CGNode, Lock> {
		public Problem(CallGraph cg,
				TFProvider transferFunctionProvider) {
			super(cg, transferFunctionProvider);
		}
	}
	
	public Solver(CallGraph callGraph, HashMap<IMethod, HashMap<SSAInstruction, IntSetVariable>> intraProceduralLocks) {
		super(new Problem(callGraph, new TFProvider(callGraph, intraProceduralLocks)));
	}

	@Override
	protected Lock makeNodeVariable(CGNode n, boolean IN) {
		return new Lock();
	}

	@Override
	protected Lock makeEdgeVariable(CGNode src, CGNode dst) {
		return new Lock();
	}
}
