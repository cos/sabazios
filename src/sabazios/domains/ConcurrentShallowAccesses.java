package sabazios.domains;

import java.util.LinkedHashSet;
import java.util.Set;

public class ConcurrentShallowAccesses extends ConcurrentAccesses<ConcurrentAccess<?>> {
	private static final long serialVersionUID = 4508447631518378345L;

	public static ConcurrentShallowAccesses rippleUp(ConcurrentFieldAccesses cfas) {
		ConcurrentShallowAccesses cas = new ConcurrentShallowAccesses();
		for (Loop t : cfas.keySet()) {
			Set<ConcurrentFieldAccess> s = cfas.get(t);
			int i = 0;
			if (!cas.containsKey(t))
				cas.put(t, new LinkedHashSet<ConcurrentAccess<?>>());
			Set<ConcurrentAccess<?>> casForThread = cas.get(t);
			for (ConcurrentFieldAccess ca : s) {
				ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) ca;
				System.out.println(i++);
				ConcurrentAccess<?> rippledUp = cfa.rippleUp();
				
				boolean mergedWithExisting = false;
				for (ConcurrentAccess<?> concurrentAccess : casForThread) {
					if (concurrentAccess instanceof ConcurrentShallowAccess && rippledUp instanceof ConcurrentShallowAccess && concurrentAccess.sameTarget(rippledUp)) {
						ConcurrentShallowAccess concSAccess = (ConcurrentShallowAccess) concurrentAccess;
						concSAccess.alphaAccesses.addAll(rippledUp.alphaAccesses);
						concSAccess.betaAccesses.addAll(rippledUp.betaAccesses);
						concSAccess.objects.addAll(((ConcurrentShallowAccess) rippledUp).objects);
						mergedWithExisting = true;
						break;
					}
				}
				if(!mergedWithExisting)
					casForThread.add(rippledUp);
			}
		}
		return cas;
	}
}
