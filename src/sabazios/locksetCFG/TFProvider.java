package sabazios.locksetCFG;

import sabazios.util.IntSetVariable;
import sabazios.util.IntSetVariableIdentity;
import sabazios.util.IntSetVariableIntersection;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

public class TFProvider implements ITransferFunctionProvider<IExplodedBasicBlock, IntSetVariable> {

	@Override
	public UnaryOperator<IntSetVariable> getNodeTransferFunction(IExplodedBasicBlock node) {
		SSAInstruction instruction = node.getInstruction();
		if (instruction instanceof SSAMonitorInstruction) {
			SSAMonitorInstruction i = (SSAMonitorInstruction) instruction;
			return MonitorTransferFunction.get(i.getRef(), i.isMonitorEnter());
		} else {
			return IntSetVariableIdentity.instance;
		}
	}

	@Override
	public boolean hasNodeTransferFunctions() {
		return true;
	}

	@Override
	public boolean hasEdgeTransferFunctions() {
		return false;
	}

	@Override
	public UnaryOperator<IntSetVariable> getEdgeTransferFunction(IExplodedBasicBlock src, IExplodedBasicBlock dst) {
		return null;
	}

	@Override
	public AbstractMeetOperator<IntSetVariable> getMeetOperator() {
		return IntSetVariableIntersection.instance;
	}

}
