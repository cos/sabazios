package sabazios.domains;

import java.util.LinkedHashSet;

import sabazios.util.U;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ConcurrentShallowAccess extends ConcurrentAccess<ObjectAccess> {
	public final LinkedHashSet<InstanceKey> objects = new LinkedHashSet<InstanceKey>();

	public ConcurrentShallowAccess(Loop t) {
		super(t);
	}

	public void gatherObjects() {
		for (ObjectAccess oa : alphaAccesses)
			objects.add(oa.o);

		for (ObjectAccess oa : betaAccesses)
			objects.add(oa.o);
	}

	@Override
	public boolean sameTarget(ConcurrentAccess<?> obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != this.getClass())
			return false;

		ConcurrentShallowAccess other = (ConcurrentShallowAccess) obj;
		for (InstanceKey o1 : this.objects) {
			for (InstanceKey o2 : other.objects)
				if (U.sameExceptIteration(o1, o2)) {
					return true;
				}
		}
		return false;
	}

	// @Override
	// public boolean sameTarget(ConcurrentAccess<?> obj) {
	// if (obj == null)
	// return false;
	// if (obj.getClass() != this.getClass())
	// return false;
	//
	// ConcurrentShallowAccess other = (ConcurrentShallowAccess) obj;
	// for (InstanceKey o1 : this.objects) {
	// for (InstanceKey o2 : other.objects)
	// if (o1.toString().equals(o2.toString())) {
	// return true;
	// }
	// }
	// return false;
	// }

	@Override
	public String toString(String linePrefix) {
		StringBuffer s = new StringBuffer();
		s.append(linePrefix);
		s.append("Shallow | ");
		if (this.objects.size() == 1) {
			s.append("Object : ");
			s.append(U.tos(this.objects.iterator().next()));
		} else {
			s.append("Objects : ");
			for (InstanceKey o : objects) {
				s.append("\n");
				s.append(linePrefix);
				s.append("  ");
				s.append(U.tos(o));
			}
		}
		return postfixToString(linePrefix, s);
	}
}
