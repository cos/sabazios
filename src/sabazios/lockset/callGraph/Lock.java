package sabazios.lockset.callGraph;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.lockIdentity.Dereferences;
import sabazios.util.CodeLocation;
import sabazios.util.IntSetVariable;
import sabazios.util.U;

import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.util.collections.ArraySet;
import com.ibm.wala.util.graph.impl.NodeWithNumber;

public class Lock extends NodeWithNumber implements IVariable<Lock> {
	private static int nextHashCode = 0;
	private LinkedHashMap<CGNode, IntSetVariable> locks = null;
	private int orderNumber;
	private final int hashCode;
	
	public static class Individual {
		public final CGNode n;
		public final int v;
		public Individual(CGNode n, int v) {
			this.n = n;
			this.v = v;
		}
	}

	public Lock(boolean top) {
		hashCode = nextHash();
		if (!top)
			locks = new LinkedHashMap<CGNode, IntSetVariable>();
	}

	public boolean containsNode(CGNode n) {
		return locks.containsKey(n);
	}

	public IntSetVariable get(CGNode n) {
		return locks.get(n);
	}

	public Lock() {
		this(false);
	}

	public boolean isTop() {
		return this.locks == null;
	}

	@Override
	public void copyState(Lock v) {
		if (v.isTop())
			this.locks = null;
		else {
			this.locks = new LinkedHashMap<CGNode, IntSetVariable>();
			for (CGNode n : v.locks.keySet())
				this.locks.put(n, v.locks.get(n));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != Lock.class)
			return false;

		Lock other = (Lock) obj;
		if (this.isTop())
			if (other.isTop())
				return true;
			else
				return false;
		else
			return locks.equals(other.locks);
	}

	public void intersect(Lock other) {
		if (this.isTop()) {
			this.copyState(other);
			return;
		}
		if (other.isTop())
			return;

		LinkedHashMap<CGNode, IntSetVariable> newLocks = new LinkedHashMap<CGNode, IntSetVariable>();
		for (CGNode n : this.locks.keySet())
			if (other.locks.keySet().contains(n)) {
				this.locks.get(n).intersect(other.locks.get(n));
				if (!this.locks.get(n).isEmpty())
					newLocks.put(n, this.locks.get(n));
			}
		this.locks = newLocks;
	}

	@Override
	public Object clone() {
		Lock newLock = new Lock();
		newLock.copyState(this);
		return newLock;
	}

	@Override
	public final int hashCode() {
		if (this.isTop())
			return -1;
		else
			return hashCode;
	}

	@Override
	public int getOrderNumber() {
		return orderNumber;
	}

	@Override
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public static synchronized int nextHash() {
		return nextHashCode++;
	}

	public void addNewVars(CGNode src, IntSetVariable var) {
		if (var.isEmpty())
			return;
		// handle the case the method is part of a recursion
		IntSetVariable selfLock = this.locks.get(src);
		if (selfLock != null)
			selfLock.union(var);
		else
			this.locks.put(src, var);
	}

	public boolean isEmpty() {
		if (isTop())
			return false;
		return locks.isEmpty();
	}

	@Override
	public String toString() {
		if (isTop())
			return "TOP";
		else {
			StringBuffer s = new StringBuffer();
			s.append("{ ");
			for (CGNode n : locks.keySet()) {
				// s.append(n);
				IntSetVariable locValues = locks.get(n);
				for (Integer v : locValues) {
					if (v == -1) {
						s.append("S : ");
						s.append(n.getMethod().getDeclaringClass().getName());
					} else {

						s.append(v);
						s.append(": ");
						Set<SSAMonitorInstruction> monitorAquires = getMonitorAquires(n, v);

						if (monitorAquires.size() == 0)
							if (v == 1)
								s.append(CodeLocation.make(n));
							else
								throw new RuntimeException("ups...");
						if (monitorAquires.size() == 1)
							s.append(CodeLocation.make(n, monitorAquires.iterator().next()));
						if (monitorAquires.size() > 1) {
							s.append("MultipleLockEnters{");
							for (SSAMonitorInstruction ssaMonitorInstruction : monitorAquires) {
								s.append(CodeLocation.make(n, ssaMonitorInstruction));
								s.append(",");
							}
							s.delete(s.length() - 1, s.length());
							s.append("}");
						}
						if(U.detailedResults) {
							s.append(" "+Dereferences.get(n, v));
						}
						s.append(" , ");
					}
				}
			}
			if (s.toString().contains(" , "))
				s.delete(s.length() - 3, s.length());
			s.append(" }");
			return s.toString();
		}
	}

	private Set<SSAMonitorInstruction> getMonitorAquires(CGNode n, Integer v) {
		ArraySet<SSAMonitorInstruction> ms = new ArraySet<SSAMonitorInstruction>();

		SSAInstruction[] instructions = n.getIR().getInstructions();
		for (SSAInstruction i : instructions) {
			if (i instanceof SSAMonitorInstruction) {
				SSAMonitorInstruction mi = (SSAMonitorInstruction) i;
				if (mi.isMonitorEnter() && mi.getRef() == v)
					ms.add(mi);
			}
		}
		return ms;
	}

	public Set<Individual> getIndividualLocks() {
		LinkedHashSet<Individual> l = new LinkedHashSet<Lock.Individual>();
		for (CGNode n :this.locks.keySet()) {
			IntSetVariable intSetVariable = this.locks.get(n);
			for (Integer v : intSetVariable) {
				l.add(new Individual(n,v));
			}
		}
		
		return l;
	}
}