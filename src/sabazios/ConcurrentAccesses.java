package sabazios;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import sabazios.domains.ConcurrentAccess;
import sabazios.domains.Loop;
import sabazios.util.U;

public class ConcurrentAccesses extends HashMap<Loop, TreeSet<ConcurrentAccess>>{
	private static final long serialVersionUID = -962627971686357883L;
	protected final RaceAnalysis a;

	public ConcurrentAccesses(RaceAnalysis a) {
		this.a = a;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (Loop t : this.keySet()) {
			s.append("Loop: " + t);
			for (ConcurrentAccess concurrentAccess : this.get(t)) {
				s.append("\n");
				s.append(concurrentAccess.toString("   "));
			}
			s.append("\n");
		}
		return s.toString();
	}

	public int getNoPairs() {
		int n = 0;
		for (TreeSet<ConcurrentAccess> accs : this.values()) {
			for (ConcurrentAccess concurrentAccess : accs) {
				n += concurrentAccess.getNoPairs();
			}
		}
		return n;
	}

	public void reduceNonConcurrentAndSimilarLooking() {
		// remove read this that don't have write this
		Iterator<Loop> abstractThreadsIt = this.keySet().iterator();
		while (abstractThreadsIt.hasNext()) {
			Loop t = abstractThreadsIt.next();
			TreeSet<ConcurrentAccess> cas = this.get(t);
			TreeSet<ConcurrentAccess> newCAS = new TreeSet<ConcurrentAccess>();
			for (ConcurrentAccess concAccess : cas) {
				ConcurrentAccess ca = (ConcurrentAccess) concAccess;
				// do not keep conccurent this that actually don't have
				// conccurency
				if (ca.writeAccesses.isEmpty() || ca.otherAccesses.isEmpty())
					continue;
	
				// do not keep duplicate this where the write to an object
				// of the main thread looks identical to the write
				// to an object of the check thread. this does not add value
				String s = ca.toString();
				boolean shouldRemove = false;
				for (ConcurrentAccess concurrentAccess : cas) {
					ConcurrentAccess ca1 = (ConcurrentAccess) concurrentAccess;
					if (ca != ca1 && !U.isMainContext(ca.o) && s.equals(ca1.toString())) {
						shouldRemove = true;
						break;
					}
				}
				if (shouldRemove)
					continue;
				
				newCAS.add(ca);
			}
	
		
			if (newCAS.isEmpty())
				abstractThreadsIt.remove();
			else {
				this.put(t, newCAS);
			}
		}
	}

	/**
	 * This uses the Locks object to tell each method which lock protects it.
	 * This way we don't have to query the outside Locks object each time we need to see 
	 * which lock protects a certain access. 
	 * @param raceAnalysis
	 */
	public void distributeLocks() {
		for (TreeSet<ConcurrentAccess> cas : this.values()) {
			for (ConcurrentAccess concurrentAccess : cas) {
				concurrentAccess.distributeLocks(a.locks);
			}
		}
	}
}
