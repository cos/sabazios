package sabazios.lockset;

import java.util.LinkedHashSet;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAMonitorInstruction;

import sabazios.deref.DerefRep;
import sabazios.deref.Dereferences;
import sabazios.util.CodeLocation;
import sabazios.util.IntSetVariable;
import sabazios.util.U;

public class LockSet extends LinkedHashSet<Lock> {
	private static final long serialVersionUID = 7287923114066974811L;

	public DerefRep commonUniqueDeref(LockSet other) {
		if (other == null)
			throw new NullPointerException();
		for (Lock l1 : this)
			for (Lock l2 : other) {
				DerefRep uniqueDeref1 = l1.uniqueDeref();
				if (uniqueDeref1 !=null && uniqueDeref1.same(l2.uniqueDeref()))
					return uniqueDeref1;
			}
		return null;
	}
}
