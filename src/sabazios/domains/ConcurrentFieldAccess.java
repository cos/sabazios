package sabazios.domains;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sabazios.A;
import sabazios.util.CGNodeUtil;
import sabazios.util.Relation;
import sabazios.util.Tuple;
import sabazios.util.U;
import sabazios.wala.CS;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class ConcurrentFieldAccess extends ConcurrentAccess {
	public final InstanceKey o;	
	public final IField f;

	public ConcurrentFieldAccess(Loop t, InstanceKey o, IField f) {
		super(t);
		if(f == null)
			throw new NullPointerException("f cannot be null");
		this.o = o;
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
		for (ObjectAccess w : this.alphaAccesses) {
			was.addAll(rippleUp((FieldAccess) w));
		}
		for (ObjectAccess w : this.betaAccesses) {
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

		ConcurrentShallowAccess ca = new ConcurrentShallowAccess(t);

		ca.alphaAccesses.addAll(was);
		ca.betaAccesses.addAll(oas);

		ca.gatherObjects();
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
	public boolean sameTarget(ConcurrentAccess obj) {
		if(obj == null)
			return false;
		if(obj.getClass() != this.getClass())
			return false;
		
		ConcurrentFieldAccess other = (ConcurrentFieldAccess) obj;
		
		if(!f.equals(other.f))
			return false;
		
		if(o instanceof AllocationSiteInNode && other.o instanceof AllocationSiteInNode) {
			AllocationSiteInNode o1 = (AllocationSiteInNode) o;
			AllocationSiteInNode o2 = (AllocationSiteInNode) other.o;
			CGNode n1 = o1.getNode();
			CGNode n2 = o2.getNode();
			return CGNodeUtil.equalsExcept(n1, n2, CS.MAIN_ITERATION);
				
		} else
			return o.equals(other.o);
	}
	
	public void checkConsistency() {
		if(o!=null){
		for (ObjectAccess oa : alphaAccesses) 
			if(!o.equals(oa.o))
				throw new RuntimeException("Inconsistency");
		
		for (ObjectAccess oa : betaAccesses) 
			if(!o.equals(oa.o))
				throw new RuntimeException("Inconsistency");
		} else {
			for (ObjectAccess oa : alphaAccesses) 
				if(oa.o != null)
					throw new RuntimeException("Inconsistency");
			
			for (ObjectAccess oa : betaAccesses) 
				if(oa.o != null)
					throw new RuntimeException("Inconsistency");
		}
	}
}
