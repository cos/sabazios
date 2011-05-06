package sabazios.locksetCallGraph;

import java.util.HashMap;

import sabazios.util.IntSetVariable;

import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.impl.NodeWithNumber;

import edu.illinois.reLooper.sabazios.CodeLocation;

public class Lock extends NodeWithNumber implements IVariable<Lock> {
	private static int nextHashCode = 0;
	private HashMap<CGNode, IntSetVariable> locks = null;
	private int orderNumber;
	private final int hashCode;

	public Lock(boolean top) {
		hashCode = nextHash();
		if (!top)
			locks = new HashMap<CGNode, IntSetVariable>();
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
			this.locks = new HashMap<CGNode, IntSetVariable>();
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

		HashMap<CGNode, IntSetVariable> newLocks = new HashMap<CGNode, IntSetVariable>();
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
		if(var.isEmpty())
			return;
		// handle the case the method is part of a recursion
		IntSetVariable selfLock = this.locks.get(src);
		if(selfLock != null)
			selfLock.union(var);
		else
			this.locks.put(src, var);
	}

	public boolean isEmpty() {
		if(isTop())
			return false;
		return locks.isEmpty();
	}
	
	@Override
	public String toString() {
		if(isTop())
			return "TOP";
		else {
			StringBuffer s = new StringBuffer();
			s.append("{ ");
			for(CGNode n: locks.keySet()) {
				s.append(n.getMethod());
				s.append(" -> ");
				s.append(locks.get(n));
				s.append(" , ");
			}
			s.append(" }");
			return s.toString();
		}
	}
}