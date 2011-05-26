package sabazios.lockset;

import java.util.Set;

import sabazios.deref.DerefRep;
import sabazios.deref.Dereferences;
import sabazios.util.CodeLocation;
import sabazios.util.U;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.util.collections.ArraySet;

public class Lock {
	public final CGNode n;
	public final int v;
	private Set<DerefRep> deref;

	public Lock(CGNode n, int v) {
		this.n = n;
		this.v = v;
	}

	public Set<DerefRep> deref() {
		if (deref == null)
			deref = Dereferences.get(n, v);
		return deref;
	}

	public DerefRep uniqueDeref() {
		if (deref() == null || deref().size() > 1)
			return null;
		else
			return deref.iterator().next();
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		if (v == -1) {
			s.append("S : ");
			s.append(n.getMethod().getDeclaringClass().getName());
		} else {
			s.append(v);
			s.append(": ");
			Set<SSAMonitorInstruction> monitorAquires = getMonitorAquires();

			if (monitorAquires.size() == 0)
				if (v == 1)
					s.append(CodeLocation.make(n));
				else
					throw new RuntimeException("ups...");
			if (monitorAquires.size() == 1)
				s.append(CodeLocation.make(n, monitorAquires.iterator().next()));
			if (monitorAquires.size() > 1) {
				s.append("MultipleLockEnters{");
				for (SSAMonitorInstruction ssaMonitorInstruction : monitorAquires) {
					s.append(CodeLocation.make(n, ssaMonitorInstruction));
					s.append(",");
				}
				s.delete(s.length() - 1, s.length());
				s.append("}");
			}
			if (U.detailedResults) {
				s.append(" " + deref);
			}
		}
		return s.toString();
	}
	
	private Set<SSAMonitorInstruction> getMonitorAquires() {
		ArraySet<SSAMonitorInstruction> ms = new ArraySet<SSAMonitorInstruction>();

		SSAInstruction[] instructions = n.getIR().getInstructions();
		for (SSAInstruction i : instructions) {
			if (i instanceof SSAMonitorInstruction) {
				SSAMonitorInstruction mi = (SSAMonitorInstruction) i;
				if (mi.isMonitorEnter() && mi.getRef() == v)
					ms.add(mi);
			}
		}
		return ms;
	}
}