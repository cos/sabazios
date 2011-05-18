package sabazios.lockIdentity;

import java.util.Iterator;

import sabazios.util.CodeLocation;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

public class ValueNode {
	final CGNode n;
	final int v;

	public ValueNode(CGNode n, int v) {
		this.n = n;
		this.v = v;
	}

	@Override
	public String toString() {
		String string = ((n == null) ? "N" : (CodeLocation.make(n).toString())) + "\\n" + v;
		if (n != null) {
			String variableName = CodeLocation.variableName(v, n, n.getIR().getInstructions().length - 1);
			if (variableName != null) 
				string += " : " + variableName;
		}
		return string;

	}
}
