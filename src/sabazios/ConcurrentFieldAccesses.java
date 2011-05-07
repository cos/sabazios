package sabazios;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.Loop;
import sabazios.domains.WriteFieldAccess;
import sabazios.util.U;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ConcurrentFieldAccesses extends ConcurrentAccesses {
	private static final long serialVersionUID = 8609685673623357446L;

	public ConcurrentFieldAccesses(RaceAnalysis a) {
		super(a);
	}

	public void compute() {

		// add all write this
		for (Loop t : a.w.keySet()) {
			this.put(t, new TreeSet<ConcurrentAccess>());
			HashMap<InstanceKey, HashSet<WriteFieldAccess>> localAccesses = a.w.get(t);
			for (InstanceKey o : localAccesses.keySet()) {
				HashSet<WriteFieldAccess> writes = localAccesses.get(o);
				for (WriteFieldAccess w : writes) {
					ConcurrentFieldAccess ca = get(t, o, w.f);
					ca.writeAccesses.add(w);
				}
			}
		}

		// add all other this
		for (Loop t : this.keySet()) {
			HashMap<InstanceKey, HashSet<FieldAccess>> localAccesses = a.o.get(t);
			for (InstanceKey o : localAccesses.keySet()) {
				HashSet<FieldAccess> others = localAccesses.get(o);
				for (FieldAccess oa : others) {
					ConcurrentFieldAccess ca = get(t, o, oa.f);
					ca.otherAccesses.add(oa);
				}
			}
		}
		
		reduceNonConcurrentAndSimilarLooking();
	}

	@SuppressWarnings("unused")
	private void debugPrint(TreeSet<ConcurrentAccess> newCAS) {
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
					System.out.println("     "+U.tos(object2));
				}
			}
		}
	}

	private ConcurrentFieldAccess get(Loop t, InstanceKey o, IField f) {
		if (!this.containsKey(t))
			return null;

		TreeSet<ConcurrentAccess> localAccesses = this.get(t);
		for (ConcurrentAccess ca : localAccesses) {
			ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) ca;
			if (cfa.f.equals(f) && ((ca.o == null && o == null) || (ca.o != null && ca.o.equals(o))))
				return cfa;
		}
		ConcurrentFieldAccess ca = new ConcurrentFieldAccess(t, o, f);
		this.get(t).add(ca);
		return ca;
	}
	
	public ConcurrentAccesses rippleUp() {
		ConcurrentAccesses cas = new ConcurrentAccesses(a);
		for (Loop t : this.keySet()) {
			TreeSet<ConcurrentAccess> s = this.get(t);
			int i = 0;
			for (ConcurrentAccess ca : s) {
				ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) ca;
				System.out.println(i++);
				ConcurrentAccess rippledUp = cfa.rippleUp(a);
				if (!cas.containsKey(t))
					cas.put(t, new TreeSet<ConcurrentAccess>());
				TreeSet<ConcurrentAccess> treeSet = cas.get(t);
				if (treeSet.contains(rippledUp))
					for (ConcurrentAccess concurrentAccess : treeSet) {
						if (concurrentAccess.equals(rippledUp)) {
							concurrentAccess.writeAccesses.addAll(rippledUp.writeAccesses);
							concurrentAccess.otherAccesses.addAll(rippledUp.otherAccesses);
							break;
						}
					}
				else
					treeSet.add(rippledUp);
			}
		}
		return cas;
	}
}
