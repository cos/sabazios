package sabazios.mustAlias;
import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

public class IntraProceduralSolver extends DataflowSolver<IExplodedBasicBlock, ObjectVariable> {
	
	public static class Problem extends BasicFramework<IExplodedBasicBlock, ObjectVariable> {
		public Problem(ExplodedControlFlowGraph flowGraph,
				TFProvider transferFunctionProvider) {
			super(flowGraph, transferFunctionProvider);
		}
	}
	
	public IntraProceduralSolver(ExplodedControlFlowGraph flowGraph) {
		super(new Problem(flowGraph, new TFProvider()));
	}

	@Override
	protected ObjectVariable makeNodeVariable(IExplodedBasicBlock n, boolean IN) {
		return new ObjectVariable();
	}

	@Override
	protected ObjectVariable makeEdgeVariable(IExplodedBasicBlock src, IExplodedBasicBlock dst) {
		return null;
	}
}
