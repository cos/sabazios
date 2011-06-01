package sabazios.domains;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import sabazios.A;
import sabazios.lockset.LockSet;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;

public abstract class ObjectAccess {
	public final CGNode n;
	public final InstanceKey o;
	public final SSAInstruction i;
	public LockSet l;

	public ObjectAccess(CGNode n, SSAInstruction i, InstanceKey o) {
		this.n = n;
		this.i = i;
		this.o = o;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != this.getClass())
			return false;
		ObjectAccess other = (ObjectAccess) obj;
		return (new EqualsBuilder()).append(this.n,other.n).append(this.i,other.i).append(o,other.o).isEquals();
	}

	@Override
	public final int hashCode() {
		return new HashCodeBuilder(37,67).toHashCode();

		// this is better but more verbose
		// return new HashCodeBuilder(37,
		// 67).append(n.toString()).append(U.toString(o)).append(i.toString()).toHashCode();
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public abstract String toString(boolean b);

	protected String contextString() {
		return ""; //(U.detailedResults ? "Context: " + U.tos(n.getContext()) : "") ;
	}

	public void updateLock() {
		l = A.locks.get(n, i);
  }
}
