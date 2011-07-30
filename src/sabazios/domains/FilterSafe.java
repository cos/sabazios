package sabazios.domains;

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
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;

public class FilterSafe {
	public static void filter(A a, ConcurrentFieldAccesses races) {
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
						if (commonUniqueDeref != null && noWritesToOurBelovedObjects(a, commonUniqueDeref)) 
							iter.remove();
					}
				}
				if(ca.betaAccesses.size() == 0)
					iterca.remove();
			}
		}
	}	

	private static boolean noWritesToOurBelovedObjects(A a, DerefRep derefRep) {
		Context c = derefRep.peek().n.getContext();
		if(!(c instanceof FlexibleContext)) {
			//TODO
//			System.out.println("PROBLEM HERE! this context should be flexible "+c);
			return false;
		}
			
		FlexibleContext context = (FlexibleContext)c;
		CGNode cgn = (CGNode) context.getItem(CS.OPERATOR_CALLER);
		for (Deref d : derefRep) {
			LocalPointerKey pk = a.pointerForValue.get(d.n, d.v);
			if(pk == null)
				continue;
			Iterator<Object> iks = a.heapGraph.getSuccNodes(pk);
			while (iks.hasNext()) {
				InstanceKey ik = (InstanceKey) iks.next();
				for (Loop loop : a.alphaAccesses.keySet()) {
					if (loop.operatorCaller.equals(cgn)) {
						Map<InstanceKey, Set<WriteFieldAccess>> hashMap = a.alphaAccesses.get(loop);
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
