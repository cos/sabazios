package sabazios.lockIdentity;

import com.ibm.wala.ipa.callgraph.CGNode;

public class ValueNode {
	final CGNode n;
	final int v;
	
	public ValueNode(CGNode n, int v) {
		this.n = n;
		this.v = v;
	}
}
