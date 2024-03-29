package sabazios.domains;

import sabazios.util.CodeLocation;
import sabazios.util.U;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;


public class ShallowAccess extends ObjectAccess {
	public ShallowAccess(CGNode n, SSAInstruction i, InstanceKey o) {
		super(n, i, o);
	}

	@Override
	public String toString(boolean withObj) {
		return "" + CodeLocation.make(n, i) + contextString() + (withObj? " Object: "+ U.tos(o):"");
	}	
}
