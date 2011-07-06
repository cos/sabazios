package sabazios.lockset.callGraph;

import java.util.Map;

import sabazios.A;
import sabazios.util.IntSetVariable;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.SSAInstruction;

public class Solver extends DataflowSolver<CGNode, LockSetVariable> {

	private final A a;

	public static class Problem extends BasicFramework<CGNode, LockSetVariable> {
		private final A a;

		public Problem(A a, 
				TFProvider transferFunctionProvider) {
			super(a.callGraph, transferFunctionProvider);
			this.a = a;
		}
	}
	
	public Solver(A a, Map<IMethod, Map<SSAInstruction, IntSetVariable>> intraProceduralLocks) {
		super(new Problem(a, new TFProvider(a, intraProceduralLocks)));
		this.a = a;
	}

	@Override
	protected LockSetVariable makeNodeVariable(CGNode n, boolean IN) {
		return new LockSetVariable(a);
	}

	@Override
	protected LockSetVariable makeEdgeVariable(CGNode src, CGNode dst) {
		return new LockSetVariable(a);
	}

	@Override
	protected LockSetVariable[] makeStmtRHS(int size) {
		return new LockSetVariable[size];
	}
}
