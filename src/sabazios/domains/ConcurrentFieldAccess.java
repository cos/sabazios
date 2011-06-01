package sabazios.domains;

import java.util.LinkedHashSet;

import sabazios.util.U;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ConcurrentFieldAccess extends ConcurrentAccess<FieldAccess> {
	public final InstanceKey o;
	public final IField f;

	public ConcurrentFieldAccess(Loop t, InstanceKey o, IField f) {
		super(t);
		if (f == null)
			throw new NullPointerException("f cannot be null");
		this.o = o;
		this.f = f;
	}

	@Override
	public String toString() {
		return toString("");
	}

	@Override
	public String toString(String linePrefix) {
		StringBuffer s = new StringBuffer();
		s.append(linePrefix);
		if (o != null) {
			s.append("Object : ");
			s.append(U.tos(o));
		} else {
			s.append("Class: ");
			s.append(f.getDeclaringClass().getName());
		}
		return postfixToString(linePrefix, s);
	}

	public ConcurrentAccess<? extends ObjectAccess> rippleUp() {
		LinkedHashSet<ObjectAccess> was = new LinkedHashSet<ObjectAccess>();
		LinkedHashSet<ObjectAccess> oas = new LinkedHashSet<ObjectAccess>();
		for (FieldAccess w : this.alphaAccesses) 
			was.addAll(w.rippleUp());
		
		for (FieldAccess w : this.betaAccesses) 
			oas.addAll(w.rippleUp());
		
		if(was.size() == 0 || oas.size() == 0)
			throw new RuntimeException("see what happens here");
		
		boolean ugly = false;
		for (ObjectAccess oa : was)
			if (!(oa instanceof FieldAccess)) {
				ugly = true;
				break;
			}
		for (ObjectAccess oa : oas)
			if (!(oa instanceof FieldAccess)) {
				ugly = true;
				break;
			}

		if (!ugly)
			return this;

		ConcurrentShallowAccess ca = new ConcurrentShallowAccess(t);

		ca.alphaAccesses.addAll(was);
		ca.betaAccesses.addAll(oas);

		ca.gatherObjects();
		return ca;
	}

	@Override
	public boolean sameTarget(ConcurrentAccess<?> obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != this.getClass())
			return false;

		ConcurrentFieldAccess other = (ConcurrentFieldAccess) obj;

		if (!f.equals(other.f))
			return false;

		return U.sameExceptIteration(o, other.o);
	}

	public void checkConsistency() {
		if (o != null) {
			for (ObjectAccess oa : alphaAccesses)
				if (!o.equals(oa.o))
					throw new RuntimeException("Inconsistency");

			for (ObjectAccess oa : betaAccesses)
				if (!o.equals(oa.o))
					throw new RuntimeException("Inconsistency");
		} else {
			for (ObjectAccess oa : alphaAccesses)
				if (oa.o != null)
					throw new RuntimeException("Inconsistency");

			for (ObjectAccess oa : betaAccesses)
				if (oa.o != null)
					throw new RuntimeException("Inconsistency");
		}
	}
}
