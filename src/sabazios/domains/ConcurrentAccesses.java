package sabazios.domains;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.A;

public abstract class ConcurrentAccesses<T extends ConcurrentAccess<?>> extends LinkedHashMap<Loop, Set<T>>{
	private static final long serialVersionUID = -962627971686357883L;
	private int noUniquePrintedPairs = 0;

	public ConcurrentAccesses() {
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		noUniquePrintedPairs = 0;
		for (Loop t : this.keySet()) {
			s.append("Loop: " + t);
			LinkedHashSet<String> stringReps = new LinkedHashSet<String>();
			
			for (ConcurrentAccess<?> concurrentAccess : this.get(t)) {
				stringReps.add(concurrentAccess.toString("   "));
				noUniquePrintedPairs += concurrentAccess.getNoUniquePrintedPairs();
			}
			for (String srep : stringReps) {
				s.append("\n");
				s.append(srep);
			}
			
			s.append("\n");
		}
		return s.toString();
	}

	public int getNoPairs() {
		int n = 0;
		for (Set<T> accs : this.values()) {
			for (ConcurrentAccess<?> concurrentAccess : accs) {
				n += concurrentAccess.getNoPairs();
			}
		}
		return n;
	}
	
	public int getNoUniquePrintedPairs() {
		return noUniquePrintedPairs;
	}

	/**
	 * This uses the Locks object to tell each method which lock protects it.
	 * This way we don't have to query the outside Locks object each time we need to see 
	 * which lock protects a certain access. 
	 * @param raceAnalysis
	 */
	public void distributeLocks() {
		for (Set<T> cas : this.values()) {
			for (ConcurrentAccess<?> concurrentAccess : cas) {
				concurrentAccess.distributeLocks(A.locks);
			}
		}
	}
	
	// do not keep ConccurrentAccesses that actually don't have concurrency
	public void reduceNonConcurrent() {
		// remove read this that don't have write this
		Iterator<Loop> loops = this.keySet().iterator();
		while (loops.hasNext()) {
			Loop t = loops.next();
			Set<T> cas = (Set<T>) this.get(t);
			Set<T> newCAS = new LinkedHashSet<T>();
			for (T ca : cas) {
				if (!ca.alphaAccesses.isEmpty() && !ca.betaAccesses.isEmpty())
					newCAS.add(ca);
			}

			if (newCAS.isEmpty())
				loops.remove();
			else {
				this.put(t, newCAS);
			}
		}
	}
}
