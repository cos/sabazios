package sabazios.domains;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import sabazios.A;
import sabazios.util.U;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ConcurrentFieldAccesses extends ConcurrentAccesses<ConcurrentFieldAccess> {
	private static final long serialVersionUID = 8609685673623357446L;
	private final A a;

	public ConcurrentFieldAccesses(A a) {
		this.a = a;
	}

	public void compute(AlphaAccesses alphaAccesses, BetaAccesses betaAccesses) {

		// add all write this
		for (Loop t : alphaAccesses.keySet()) {
			this.put(t, new LinkedHashSet<ConcurrentFieldAccess>());
			Map<InstanceKey, Set<WriteFieldAccess>> localAccesses = alphaAccesses.get(t);
			for (InstanceKey o : localAccesses.keySet()) {
				Set<WriteFieldAccess> writes = localAccesses.get(o);
				for (WriteFieldAccess w : writes) {
					ConcurrentFieldAccess ca = get(t, o, w.f);
					ca.alphaAccesses.add(w);
				}
			}
		}

		// add all other this
		for (Loop t : this.keySet()) {
			Map<InstanceKey, Set<FieldAccess>> localAccesses = betaAccesses.get(t);
			for (InstanceKey o : localAccesses.keySet()) {
				Set<FieldAccess> others = localAccesses.get(o);
				for (FieldAccess oa : others) {
					ConcurrentFieldAccess ca = get(t, o, oa.f);
					ca.betaAccesses.add(oa);
				}
			}
		}

		reduceNonConcurrent();
	}

	@SuppressWarnings("unused")
	private void debugPrint(Set<ConcurrentFieldAccess> newCAS) {
		InstanceKey o = newCAS.iterator().next().o;
		System.out.println("-------");
		System.out.println(U.tos(o));
		System.out.println("--");
		Iterator<Object> succNodes = a.heapGraph.getSuccNodes(o);
		while (succNodes.hasNext()) {
			Object object = (Object) succNodes.next();
			Iterator<Object> succNodes2 = a.heapGraph.getSuccNodes(object);
			System.out.println(object);
			while (succNodes2.hasNext()) {
				InstanceKey i = (InstanceKey) succNodes2.next();
				System.out.println(U.tos(i));
				System.out.println("----->");
				Iterator<Object> succNodes3 = a.heapGraph.getSuccNodes(a.heapGraph.getSuccNodes(i).next());
				while (succNodes3.hasNext()) {
					InstanceKey object2 = (InstanceKey) succNodes3.next();
					System.out.println("     " + U.tos(object2));
				}
			}
		}
	}

	private ConcurrentFieldAccess get(Loop t, InstanceKey o, IField f) {
		if (!this.containsKey(t))
			return null;

		Set<ConcurrentFieldAccess> localAccesses = (Set<ConcurrentFieldAccess>) this.get(t);
		for (ConcurrentFieldAccess ca : localAccesses) {
			ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) ca;
			if (cfa.f.equals(f) && ((ca.o == null && o == null) || (ca.o != null && ca.o.equals(o))))
				return cfa;
		}
		ConcurrentFieldAccess ca = new ConcurrentFieldAccess(t, o, f);
		this.get(t).add(ca);
		return ca;
	}

	// // do not keep duplicate this where the write to an object
	// // of the main thread looks identical to the write
	// // to an object of the check thread. this does not add value
	// public void
	// removeDuplicateWritesToObjectsAllocatedInMainAndCheckIterations() {
	// // remove read this that don't have write this
	// Iterator<Loop> loops = this.keySet().iterator();
	// while (loops.hasNext()) {
	// Loop t = loops.next();
	// Set<ConcurrentFieldAccess> cas = this.get(t);
	// Iterator<ConcurrentFieldAccess> iterator = cas.iterator();
	// while (iterator.hasNext()) {
	// ConcurrentFieldAccess ca = (ConcurrentFieldAccess) iterator.next();
	// for (ConcurrentFieldAccess ca1 : cas) {
	// if (ca != ca1 && !U.isAlphaIteration(ca.o)) {
	// iterator.remove();
	// break;
	// }
	// }
	// }
	//
	// if (cas.isEmpty())
	// loops.remove();
	// }
	// }
}
