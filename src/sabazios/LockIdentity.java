package sabazios;

import java.util.TreeSet;

import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.Loop;
import sabazios.domains.ObjectAccess;
import sabazios.domains.WriteFieldAccess;
import sabazios.lockset.callGraph.Lock;

import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.intset.IntIterator;

/**
 * Modifies RaceAnalysis, filtering initialRaces by using the locks. The result is put in deep races.
 * 
 * @author cosminradoi
 *
 */
public class LockIdentity {
	private final RaceAnalysis a;
	
	public LockIdentity(RaceAnalysis a) {
		this.a = a;
	}
	
	public void compute() {
		filter();
	}
	
	private void filter() {
		a.deepRaces = new ConcurrentFieldAccesses(a);
		for (Loop t : a.initialRaces.keySet()) {
			TreeSet<ConcurrentAccess> newAccesses = new TreeSet<ConcurrentAccess>();
			for (ConcurrentAccess ca : a.initialRaces.get(t)) {
				
//				for (ObjectAccess oa : ca.otherAccesses) {
//					FieldAccess w = (FieldAccess) oa;
//					Lock l = a.locks.get(w.n, w.i);
//					if (!l.containsNode(w.n) || (!l.get(w.n).contains(1)))
//						newCA.otherAccesses.add(w);
//				}
				
				ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) ca;
				ConcurrentFieldAccess newCA = new ConcurrentFieldAccess(t, cfa.o, cfa.f);
				for (ObjectAccess oa : cfa.writeAccesses) {
					WriteFieldAccess w = (WriteFieldAccess) oa;
					Lock l = a.locks.get(w.n, w.i);
					if (l.containsNode(w.n)) {
						boolean safe = false;
						if(l.get(w.n).contains(1))
							safe = true;
						IntIterator intIterator = l.get(w.n).intIterator();
						DefUse du = w.n.getDU();
						while (intIterator.hasNext()) {
							int lock = intIterator.next();
							SSAInstruction defLock = du.getDef(lock);
							if(defLock instanceof SSAGetInstruction) {
								SSAGetInstruction getLock = (SSAGetInstruction) defLock;
								if(getLock.getRef() == 1)
									safe = true;
							}
						}
						if(safe)
							continue;
					}
					newCA.writeAccesses.add(w);
				}

				if (!newCA.isEmpty())
					newAccesses.add(newCA);
			}
			if (!newAccesses.isEmpty())
				a.deepRaces.put(t, newAccesses);
		}
	}
}
