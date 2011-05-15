package sabazios.mustAlias;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAArrayReferenceInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

public class TFProvider implements ITransferFunctionProvider<IExplodedBasicBlock, ObjectVariable> {

	@Override
	public UnaryOperator<ObjectVariable> getNodeTransferFunction(IExplodedBasicBlock node) {
		SSAInstruction i = node.getInstruction();
		if(i instanceof SSAPutInstruction)
			return new PutTF((SSAPutInstruction) i);
		
		return null;
	}

	@Override
	public boolean hasNodeTransferFunctions() {
		return true;
	}

	@Override
	public UnaryOperator<ObjectVariable> getEdgeTransferFunction(IExplodedBasicBlock src, IExplodedBasicBlock dst) {
		return null;
	}

	@Override
	public boolean hasEdgeTransferFunctions() {
		return false;
	}

	@Override
	public AbstractMeetOperator<ObjectVariable> getMeetOperator() {
		return ObjectVariableMeet.instance;
	}

}
