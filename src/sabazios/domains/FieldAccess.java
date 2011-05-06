package sabazios.domains;

import sabazios.util.CodeLocation;
import sabazios.util.U;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;


public class FieldAccess extends ObjectAccess {
	public final IField f;
	
	public FieldAccess(CGNode n, SSAInstruction i, InstanceKey o, IField f) {
		super(n,i,o);
		if(f == null)
			throw new NullPointerException("f cannot be null");
		this.f = f;	
	}
	
	@Override
	public String toString() {
		return toString(true);
	}

	@Override
	public String toString(boolean withObj) {
		if(U.detailedResults)
			return CodeLocation.make(n, i) + n.toString() + " - ."+ f.getName() + (withObj? " / "+ (o != null?U.tos(o):f.getClass().getName()):"");
		else
			return CodeLocation.make(n, i) + " - ."+ f.getName() + (withObj? " / "+ (o != null?U.tos(o):f.getClass().getName()):"");
	}
	
}