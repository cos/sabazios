package sabazios.domains;

import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import sabazios.lockset.Lock;
import sabazios.lockset.LockSet;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;

public abstract class ObjectAccess implements Comparable<ObjectAccess> {
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
	public final int compareTo(ObjectAccess obj) {
		return this.toString(false).compareTo(obj.toString(false));

		// this is better but more verbose
		// return new CompareToBuilder().append(this.n.toString(),
		// o.n.toString())
		// .append(this.i.toString(), o.i.toString())
		// .append(U.toString(this.o), U.toString(o.o)).
		// toComparison();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != this.getClass())
			return false;
		return this.compareTo((ObjectAccess) obj) == 0;
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
}
