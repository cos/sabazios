package sabazios.domains;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.A;
import sabazios.util.CodeLocation;
import sabazios.util.Relation;
import sabazios.util.Tuple;
import sabazios.util.U;
import sabazios.wala.CS;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;


public class FieldAccess extends ObjectAccess {
	public final IField f;
	private final A a;
	
	public FieldAccess(A a, CGNode n, SSAInstruction i, InstanceKey o, IField f) {
		super(n,i,o);
		this.a = a;
		if(f == null)
			throw new NullPointerException("f cannot be null");
		this.f = f;	
	}
	
	@Override
	public String toString() {
		return toString(true);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj))
			return false;
		FieldAccess other = (FieldAccess) obj;
		return f.equals(other.f);
	}

	@Override
	public String toString(boolean withObj) {
		withObj = false;
		return CodeLocation.make(n, i) + " - ."+ f.getName() + contextString() + (withObj? " / "+ (o != null?U.tos(o):f.getClass().getName()):"")+(this.l != null && !this.l.isEmpty() ? " "+this.l : "");
	}
	
	Set<ObjectAccess> rippleUp() {
		CGNode n = this.n;
		if (U.inApplicationScope(n))
			return Sets.newHashSet((ObjectAccess) this);

		Set<ObjectAccess> sa = new LinkedHashSet<ObjectAccess>();
		Relation<CGNode, SSAInvokeInstruction> workList = new Relation<CGNode, SSAInvokeInstruction>();

		workList.add(Tuple.from(n, (SSAInvokeInstruction) null));

		Relation<CGNode, SSAInvokeInstruction> visitedAccesses = new Relation<CGNode, SSAInvokeInstruction>();
		
		while (!workList.isEmpty()) {
			Tuple<CGNode, SSAInvokeInstruction> todo = workList.any();
			n = todo.p1();
			workList.remove(todo);
			visitedAccesses.add(todo);
			Iterator<CGNode> predNodes = a.callGraph.getPredNodes(n);
			while (predNodes.hasNext()) {
				CGNode n1 = predNodes.next();
				if(CS.threadSafe(n1))
					continue;
				
				Iterator<CallSiteReference> possibleSites = a.callGraph.getPossibleSites(n1, n);
				while (possibleSites.hasNext()) {
					CallSiteReference csr = possibleSites.next();
					SSAAbstractInvokeInstruction[] calls = n1.getIR().getCalls(csr);
					for (SSAAbstractInvokeInstruction i : calls) {
						if (U.inApplicationScope(n1)) {
							if (!i.isStatic()) {
								LocalPointerKey p = a.pointerForValue.get(n1, i.getReceiver());
								Iterator<Object> succNodes = a.heapGraph.getSuccNodes(p);
								while (succNodes.hasNext()) {
									Object o1 = succNodes.next();
									sa.add(new ShallowAccess(n1, i, (InstanceKey) o1));
								}
							} else {
								sa.add(new ShallowAccess(n1, i, null));
							}
						} else {
							Tuple.Pair<CGNode, SSAInvokeInstruction> todo1 = Tuple.from(n1, (SSAInvokeInstruction) i);
							if(!visitedAccesses.contains(todo1))
								workList.add(todo1);
						}
					}
				}
			}
		}
		return sa;
	}
}