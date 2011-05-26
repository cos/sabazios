package sabazios.domains;

import java.util.LinkedHashSet;
import java.util.Set;


public class ConcurrentShallowAccesses extends ConcurrentAccesses<ConcurrentAccess> {
	private static final long serialVersionUID = 4508447631518378345L;
	
	public static ConcurrentShallowAccesses rippleUp(ConcurrentFieldAccesses cfas) {
		ConcurrentShallowAccesses cas = new ConcurrentShallowAccesses();
		for (Loop t : cfas.keySet()) {
			Set<ConcurrentFieldAccess> s = cfas.get(t);
			int i = 0;
			for (ConcurrentAccess ca : s) {
				ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) ca;
				System.out.println(i++);
				ConcurrentAccess rippledUp = cfa.rippleUp();
				if (!cas.containsKey(t))
					cas.put(t, new LinkedHashSet<ConcurrentAccess>());
				Set<ConcurrentAccess> treeSet = cas.get(t);
				
				boolean mergedWithExisting = false;
				for (ConcurrentAccess concurrentAccess : treeSet) {
					if (concurrentAccess.sameTarget(rippledUp)) {
						concurrentAccess.alphaAccesses.addAll(rippledUp.alphaAccesses);
						concurrentAccess.betaAccesses.addAll(rippledUp.betaAccesses);
						mergedWithExisting = true;
						break;
					}
				}
				if(!mergedWithExisting)
					treeSet.add(rippledUp);
			}
		}
		return cas;
	}
}
