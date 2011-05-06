package sabazios;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import sabazios.domains.Loop;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.util.U;

public class ConcurrentAccesses {
	public HashMap<Loop, TreeSet<ConcurrentAccess>> accesses = new HashMap<Loop, TreeSet<ConcurrentAccess>>();
	protected final RaceAnalysis a;

	public ConcurrentAccesses(RaceAnalysis a) {
		this.a = a;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (Loop t : accesses.keySet()) {
			s.append("Loop: " + t);
			for (ConcurrentAccess concurrentAccess : accesses.get(t)) {
				s.append("\n");
				s.append(concurrentAccess.toString("   "));
			}
			s.append("\n");
		}
		return s.toString();
	}

	public int getNoPairs() {
		int n = 0;
		for (TreeSet<ConcurrentAccess> accs : this.accesses.values()) {
			for (ConcurrentAccess concurrentAccess : accs) {
				n += concurrentAccess.getNoPairs();
			}
		}
		return n;
	}

	public void reduceNonConcurrentAndSimilarLooking() {
		// remove read accesses that don't have write accesses
		Iterator<Loop> abstractThreadsIt = accesses.keySet().iterator();
		while (abstractThreadsIt.hasNext()) {
			Loop t = abstractThreadsIt.next();
			TreeSet<ConcurrentAccess> cas = accesses.get(t);
			TreeSet<ConcurrentAccess> newCAS = new TreeSet<ConcurrentAccess>();
			for (ConcurrentAccess concAccess : cas) {
				ConcurrentAccess ca = (ConcurrentAccess) concAccess;
				// do not keep conccurent accesses that actually don't have
				// conccurency
				if (ca.writeAccesses.isEmpty() || ca.otherAccesses.isEmpty())
					continue;
	
				// do not keep duplicate accesses where the write to an object
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
				accesses.put(t, newCAS);
			}
		}
	}
}
