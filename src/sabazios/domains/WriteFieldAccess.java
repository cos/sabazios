package sabazios.domains;


import sabazios.A;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;

public class WriteFieldAccess extends FieldAccess {
	public WriteFieldAccess(A a, CGNode n, SSAInstruction i, InstanceKey o, IField f) {
		super(a, n,i,o,f);
	}
	
	@Override
	public String toString() {
		return "Write "+super.toString();
	}
	
	@Override
	public String toString(boolean withObject) {
		return "Write "+super.toString(withObject);
	}
}