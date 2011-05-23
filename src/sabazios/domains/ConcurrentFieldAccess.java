package sabazios.domains;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sabazios.A;
import sabazios.util.ComparableTuple;
import sabazios.util.Relation;
import sabazios.util.Tuple;
import sabazios.util.U;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class ConcurrentFieldAccess extends ConcurrentAccess {
	public final IField f;

	public ConcurrentFieldAccess(Loop t, InstanceKey o, IField f) {
		super(t, o);
		if(f == null)
			throw new NullPointerException("f cannot be null");
		this.f = f;
	}

	@Override
	public String toString() {
		return toString("");
	}

	@Override
	public String toString(String linePrefix) {
		StringBuffer s = new StringBuffer();
		s.append(linePrefix);
		if (o != null) {
			s.append("Object : ");
			s.append(U.tos(o));
		} else {
			s.append("Class: ");
			s.append(f.getDeclaringClass().getName());
		}
		return postfixToString(linePrefix, s);
	}

	public ConcurrentAccess rippleUp() {
		HashSet<ObjectAccess> was = new HashSet<ObjectAccess>();
		HashSet<ObjectAccess> oas = new HashSet<ObjectAccess>();
		for (ObjectAccess w : this.writeAccesses) {
			was.addAll(rippleUp((FieldAccess) w));
		}
		for (ObjectAccess w : this.otherAccesses) {
			oas.addAll(rippleUp((FieldAccess) w));
		}
		boolean ugly = false;
		for (ObjectAccess oa : was)
			if (!(oa instanceof FieldAccess)) {
				ugly = true;
				break;
			}
		for (ObjectAccess oa : oas)
			if (!(oa instanceof FieldAccess)) {
				ugly = true;
				break;
			}

		if (!ugly)
			return this;

		ConcurrentAccess ca;
		ca = new ConcurrentAccess(t, was.iterator().next().o);

		ca.writeAccesses.addAll(was);
		ca.otherAccesses.addAll(oas);

		ca.gatherOtherObjects();
		return ca;
	}

	private Set<ObjectAccess> rippleUp(FieldAccess w) {
		CGNode n = w.n;
		if (U.inApplicationScope(n))
			return Sets.newHashSet((ObjectAccess) w);

		HashSet<ObjectAccess> sa = new HashSet<ObjectAccess>();
		Relation<CGNode, SSAInvokeInstruction> workList = new Relation<CGNode, SSAInvokeInstruction>();

		workList.add(Tuple.from(n, (SSAInvokeInstruction) null));

		Relation<CGNode, SSAInvokeInstruction> visitedAccesses = new Relation<CGNode, SSAInvokeInstruction>();
		
		while (!workList.isEmpty()) {
			Tuple<CGNode, SSAInvokeInstruction> todo = workList.any();
			n = todo.p1();
			workList.remove(todo);
			visitedAccesses.add(todo);
			Iterator<CGNode> predNodes = A.callGraph.getPredNodes(n);
			while (predNodes.hasNext()) {
				CGNode n1 = predNodes.next();
				Iterator<CallSiteReference> possibleSites = A.callGraph.getPossibleSites(n1, n);
				while (possibleSites.hasNext()) {
					CallSiteReference csr = possibleSites.next();
					SSAAbstractInvokeInstruction[] calls = n1.getIR().getCalls(csr);
					for (SSAAbstractInvokeInstruction i : calls) {
						if (U.inApplicationScope(n1)) {
							if (!i.isStatic()) {
								LocalPointerKey p = A.pointerForValue.get(n1, i.getReceiver());
								Iterator<Object> succNodes = A.heapGraph.getSuccNodes(p);
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

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ConcurrentFieldAccess))
			return false;

		return this.compareTo((ConcurrentFieldAccess) obj) == 0;
	}

	@Override
	public int compareTo(ConcurrentAccess other) {
		if (!(other instanceof ConcurrentFieldAccess)) {
			return -1;
		} else {
			ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) other;
			String string = o != null ? o.toString() : "";
			String string2 = cfa.o != null ? cfa.o.toString() : "";
			String f_string = f!= null ? f.toString() : ""; 
			String cfa_f_string = cfa.f != null ? cfa.f.toString() : "";
			return ComparableTuple.from(t, string, f_string).compareTo(
					ComparableTuple.from(cfa.t, string2, cfa_f_string));
		}
	}
}
