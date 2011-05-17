package sabazios.lockIdentity;

import java.util.Iterator;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;

public class MustAliasHeapMethodSummary extends SlowSparseNumberedLabeledGraph<ValueNode, FieldEdge> {
	private final CGNode n;
	private DefUse du;

	public MustAliasHeapMethodSummary(CGNode n) {
		super(new FieldEdge());
		this.n = n;
	}
	
	void compute() {
		du = n.getDU();
		int[] parameterValueNumbers = n.getIR().getParameterValueNumbers();
		for (int v : parameterValueNumbers) {
			this.addNode(new ValueNode(n, v));
		}
		for (int v : parameterValueNumbers) {
			recurse(v);
		}
	}

	private void recurse(int v) {
		Iterator<SSAInstruction> uses = du.getUses(v);
		while (uses.hasNext()) {
			SSAInstruction i = (SSAInstruction) uses.next();
			
		}
	}
}
