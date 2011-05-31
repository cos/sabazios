package sabazios.domains;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import sabazios.A;
import sabazios.deref.Deref;
import sabazios.deref.DerefRep;
import sabazios.lockset.LockSet;
import sabazios.util.FlexibleContext;
import sabazios.wala.CS;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;

public class FilterSafe {
	public static void filter(ConcurrentFieldAccesses races) {
		for (Loop l : races.keySet()) {
			Iterator<ConcurrentFieldAccess> iterca = races.get(l).iterator();
			while (iterca.hasNext()) {
				ConcurrentFieldAccess ca = iterca.next();				
				for (ObjectAccess oa1 : ca.alphaAccesses) {
					LockSet lockSet1 = oa1.l;
					Iterator<FieldAccess> iter = ca.betaAccesses.iterator();
					while (iter.hasNext()) {
						ObjectAccess oa2 = (ObjectAccess) iter.next();
						LockSet lockSet2 = oa2.l;
						DerefRep commonUniqueDeref = lockSet1.commonUniqueDeref(lockSet2);
						if (commonUniqueDeref != null && noWritesToOurBelovedObjects(commonUniqueDeref)) 
							iter.remove();
					}
				}
				if(ca.betaAccesses.size() == 0)
					iterca.remove();
			}
		}
	}	

	private static boolean noWritesToOurBelovedObjects(DerefRep derefRep) {
		FlexibleContext context = (FlexibleContext)derefRep.peek().n.getContext();
		CGNode cgn = (CGNode) context.getItem(CS.OPERATOR_CALLER);
		for (Deref d : derefRep) {
			LocalPointerKey pk = A.pointerForValue.get(d.n, d.v);
			if(pk == null)
				continue;
			Iterator<Object> iks = A.heapGraph.getSuccNodes(pk);
			while (iks.hasNext()) {
				InstanceKey ik = (InstanceKey) iks.next();
				for (Loop loop : A.alphaAccesses.keySet()) {
					if (loop.operatorCaller.equals(cgn)) {
						Map<InstanceKey, Set<WriteFieldAccess>> hashMap = A.alphaAccesses.get(loop);
						if(hashMap.containsKey(ik))
						{
							Set<WriteFieldAccess> hashSet = hashMap.get(ik);
							for (WriteFieldAccess wf : hashSet) {
								if(wf.f.getReference().equals(d.f))
									return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
}
