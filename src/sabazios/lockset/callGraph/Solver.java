package sabazios.lockset.callGraph;

import java.util.HashMap;
import java.util.Map;

import sabazios.util.IntSetVariable;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.SSAInstruction;

public class Solver extends DataflowSolver<CGNode, LockSetVariable> {

	public static class Problem extends BasicFramework<CGNode, LockSetVariable> {
		public Problem(CallGraph cg,
				TFProvider transferFunctionProvider) {
			super(cg, transferFunctionProvider);
		}
	}
	
	public Solver(CallGraph callGraph, Map<IMethod, Map<SSAInstruction, IntSetVariable>> intraProceduralLocks) {
		super(new Problem(callGraph, new TFProvider(callGraph, intraProceduralLocks)));
	}

	@Override
	protected LockSetVariable makeNodeVariable(CGNode n, boolean IN) {
		return new LockSetVariable();
	}

	@Override
	protected LockSetVariable makeEdgeVariable(CGNode src, CGNode dst) {
		return new LockSetVariable();
	}

	@Override
	protected LockSetVariable[] makeStmtRHS(int size) {
		return new LockSetVariable[size];
	}
}
