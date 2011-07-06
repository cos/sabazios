package sabazios.lockset.callGraph;

import java.util.Iterator;
import java.util.Map;

import sabazios.A;
import sabazios.util.IntSetVariable;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;

public class TFProvider implements ITransferFunctionProvider<CGNode, LockSetVariable> {

	// private static final UnaryOperator<Lock> lockIdentity = new
	// IntSetIdentity<Lock>();
	private final Map<IMethod, Map<SSAInstruction, IntSetVariable>> intraProceduralLocks;
	private final A a;

	public TFProvider(A a,
			Map<IMethod, Map<SSAInstruction, IntSetVariable>> intraProceduralLocks) {
		this.a = a;
		this.intraProceduralLocks = intraProceduralLocks;
	}

	@Override
	public boolean hasNodeTransferFunctions() {
		return true;
	}

	@Override
	public UnaryOperator<LockSetVariable> getNodeTransferFunction(CGNode node) {
		if (node.getMethod().isSynchronized()) {
			if(!node.getMethod().isStatic()) {
				IntSetVariable var = new IntSetVariable();
				var.add(1);
				return new AddLockTransferFunction(node, var);				
			} else {
				IntSetVariable var = new IntSetVariable();
				var.add(0);
				return new AddLockTransferFunction(node, var);
			}
		} else 
			return LockIdentity.instance;
	}

	@Override
	public boolean hasEdgeTransferFunctions() {
		return true;
	}

	@Override
	public UnaryOperator<LockSetVariable> getEdgeTransferFunction(CGNode src, CGNode dst) {
		Iterator<CallSiteReference> possibleSites = a.callGraph.getPossibleSites(src, dst);
		IntSetVariable var = IntSetVariable.newTop();
		while (possibleSites.hasNext()) {
			CallSiteReference callSiteReference = possibleSites.next();
			SSAAbstractInvokeInstruction[] calls = src.getIR().getCalls(callSiteReference);
			for (SSAAbstractInvokeInstruction ii : calls) {
				IntSetVariable l = this.intraProceduralLocks.get(src.getMethod()).get(ii);
				if(l != null)
					var.intersect(l);
			}
		}
		return new AddLockTransferFunction(src, var);
	}

	@Override
	public AbstractMeetOperator<LockSetVariable> getMeetOperator() {
		return LockMeet.instance;
	}

}
