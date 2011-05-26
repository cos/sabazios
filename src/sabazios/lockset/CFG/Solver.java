package sabazios.lockset.CFG;


import sabazios.util.IntSetVariable;

import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

public class Solver extends DataflowSolver<IExplodedBasicBlock, IntSetVariable> {

	public static class Problem extends BasicFramework<IExplodedBasicBlock, IntSetVariable> {
		public Problem(ExplodedControlFlowGraph flowGraph,
				TFProvider transferFunctionProvider) {
			super(flowGraph, transferFunctionProvider);
		}
	}
	
	public Solver(ExplodedControlFlowGraph flowGraph, boolean enter) {
		super(new Problem(flowGraph, new TFProvider(enter)));
	}

	@Override
	protected IntSetVariable makeNodeVariable(IExplodedBasicBlock n, boolean IN) {
		return new IntSetVariable();
	}

	@Override
	protected IntSetVariable makeEdgeVariable(IExplodedBasicBlock src, IExplodedBasicBlock dst) {
		return null;
	}

	@Override
	protected IntSetVariable[] makeStmtRHS(int size) {
		return new IntSetVariable[size];
	}

}
