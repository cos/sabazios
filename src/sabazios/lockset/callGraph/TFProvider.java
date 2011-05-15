package sabazios.lockset.callGraph;

import java.util.HashMap;
import java.util.Iterator;

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

public class TFProvider implements ITransferFunctionProvider<CGNode, Lock> {

	//	private static final UnaryOperator<Lock> lockIdentity = new IntSetIdentity<Lock>();
	private final HashMap<IMethod, HashMap<SSAInstruction, IntSetVariable>> intraProceduralLocks;
	private final CallGraph callGraph;

	public TFProvider(CallGraph callGraph, HashMap<IMethod, HashMap<SSAInstruction, IntSetVariable>> intraProceduralLocks) {
		this.callGraph = callGraph;
		this.intraProceduralLocks = intraProceduralLocks;
	}
	@Override
	public boolean hasNodeTransferFunctions() {
		return false;
	}
	@Override
	public UnaryOperator<Lock> getNodeTransferFunction(CGNode node) {
		return null;
	}

	@Override
	public boolean hasEdgeTransferFunctions() {
		return true;
	}

	@Override
	public UnaryOperator<Lock> getEdgeTransferFunction(CGNode src, CGNode dst) {
		Iterator<CallSiteReference> possibleSites = callGraph.getPossibleSites(src, dst);
		IntSetVariable var = new IntSetVariable(true);
		while (possibleSites.hasNext()) {
			CallSiteReference callSiteReference = possibleSites.next();
			SSAAbstractInvokeInstruction[] calls = src.getIR().getCalls(callSiteReference);
			for(SSAAbstractInvokeInstruction ii : calls) {
				IntSetVariable l = this.intraProceduralLocks.get(src.getMethod()).get(ii);
				var.intersect(l);
			}
		}
		return new AddLockTransferFunction(src, var);
	}

	@Override
	public AbstractMeetOperator<Lock> getMeetOperator() {
		return LockIntersection.instance;
	}

}
