package sabazios.domains;

import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.lockset.Locks;
import sabazios.util.U;

public abstract class ConcurrentAccess {

	public final Loop t;
	public final LinkedHashSet<ObjectAccess> alphaAccesses = new LinkedHashSet<ObjectAccess>();
	public final LinkedHashSet<ObjectAccess> betaAccesses = new LinkedHashSet<ObjectAccess>();

	public ConcurrentAccess(Loop t) {
		this.t = t;
	}

	public abstract String toString(String s);

	protected String postfixToString(String linePrefix, StringBuffer s) {
		s.append("\n");
		s.append(linePrefix);
		s.append("   Write accesses:");
		accessesToString(alphaAccesses, linePrefix, s);
		s.append("\n");
		s.append(linePrefix);
		s.append("   Other accesses:");
		accessesToString(betaAccesses, linePrefix, s);
		return s.toString();
	}

	private static void accessesToString(Set<ObjectAccess> accesses, String linePrefix, StringBuffer s) {
		for (ObjectAccess w : accesses) {
			s.append("\n");
			s.append(linePrefix);
			s.append("     ");
			s.append(w.toString(U.detailedResults));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof ConcurrentAccess))
			return false;

		ConcurrentAccess other = (ConcurrentAccess) obj;
		if (!(this.t.equals(other.t) && this.alphaAccesses.equals(other.alphaAccesses) && this.betaAccesses
				.equals(other.betaAccesses)))
			return false;

		if (obj.getClass() != this.getClass())
			return false;
		return this.sameTarget(other);
	}

	@Override
	public int hashCode() {
		return t.hashCode() + 34534643;
	}

	public boolean isEmpty() {
		return this.alphaAccesses.isEmpty() || this.betaAccesses.isEmpty();
	}

	public int getNoPairs() {
		int w = this.alphaAccesses.size();
		int o = this.betaAccesses.size();
		return w * o;
	}

	public void distributeLocks(Locks locks) {
		distributeLocks(alphaAccesses, locks);
		distributeLocks(betaAccesses, locks);
	}

	private static void distributeLocks(Set<ObjectAccess> writeAccesses2, Locks locks) {
		for (ObjectAccess objectAccess : writeAccesses2) {
			objectAccess.l = locks.get(objectAccess.n, objectAccess.i);
		}
	}

	public abstract boolean sameTarget(ConcurrentAccess rippledUp);
}
