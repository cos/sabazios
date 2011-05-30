package sabazios.domains;

import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.lockset.Locks;
import sabazios.util.U;

public abstract class ConcurrentAccess<T extends ObjectAccess> {

	public final Loop t;
	public final LinkedHashSet<T> alphaAccesses = new LinkedHashSet<T>();
	public final LinkedHashSet<T> betaAccesses = new LinkedHashSet<T>();
	private int noUniquePrintedPairs = 0;

	public ConcurrentAccess(Loop t) {
		this.t = t;
	}

	public abstract String toString(String s);

	protected String postfixToString(String linePrefix, StringBuffer s) {
		noUniquePrintedPairs = 0;
		s.append("\n");
		s.append(linePrefix);
		s.append("   Write accesses:");
		noUniquePrintedPairs += accessesToString(alphaAccesses, linePrefix, s);;
		s.append("\n");
		s.append(linePrefix);
		s.append("   Other accesses:");
		noUniquePrintedPairs += accessesToString(betaAccesses, linePrefix, s);
		return s.toString();
	}

	private static <T extends ObjectAccess> int accessesToString(Set<T> accesses, String linePrefix, StringBuffer s) {
		LinkedHashSet<String> sreps = new LinkedHashSet<String>();
		for (ObjectAccess w : accesses) {
			sreps.add(w.toString(U.detailedResults));
		}
		
		for (String srep : sreps) {
			s.append("\n");
			s.append(linePrefix);
			s.append("     ");
			s.append(srep);
		}
		return sreps.size();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof ConcurrentAccess))
			return false;

		@SuppressWarnings("unchecked")
		ConcurrentAccess<T> other = (ConcurrentAccess<T>) obj;
		if(!this.t.equals(other.t))
			return false;
		if(!sameAccesses(other))
			return false;
		
		sameAccesses(other);

		if (obj.getClass() != this.getClass())
			return false;
		return this.sameTarget(other);
	}

	public boolean sameAccesses(ConcurrentAccess<T> other) {
	  return this.alphaAccesses.equals(other.alphaAccesses) && this.betaAccesses
				.equals(other.betaAccesses);
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

	private static <T extends ObjectAccess> void distributeLocks(Set<T> writeAccesses2, Locks locks) {
		for (ObjectAccess objectAccess : writeAccesses2) {
			objectAccess.l = locks.get(objectAccess.n, objectAccess.i);
		}
	}

	public abstract boolean sameTarget(ConcurrentAccess<?> rippledUp);

	public int getNoUniquePrintedPairs() {
	  return noUniquePrintedPairs ;
  }
}
