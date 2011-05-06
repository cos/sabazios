package sabazios.domains;

import java.util.HashSet;
import java.util.TreeSet;

import sabazios.util.ComparableTuple;
import sabazios.util.U;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ConcurrentAccess implements Comparable<ConcurrentAccess> {

	public final Loop t;
	public final InstanceKey o;
	public final TreeSet<ObjectAccess> writeAccesses = new TreeSet<ObjectAccess>();
	public final TreeSet<ObjectAccess> otherAccesses = new TreeSet<ObjectAccess>();
	
	public final HashSet<InstanceKey> otherO = new HashSet<InstanceKey>();
	
	public ConcurrentAccess(Loop t, InstanceKey o) {
		this.t = t;
		this.o = o;
	}

	
	public void checkConsistency() {
		if(o!=null){
		for (ObjectAccess oa : writeAccesses) 
			if(!o.equals(oa.o))
				throw new RuntimeException("Inconsistency");
		
		for (ObjectAccess oa : otherAccesses) 
			if(!o.equals(oa.o))
				throw new RuntimeException("Inconsistency");
		} else {
			for (ObjectAccess oa : writeAccesses) 
				if(oa.o != null)
					throw new RuntimeException("Inconsistency");
			
			for (ObjectAccess oa : otherAccesses) 
				if(oa.o != null)
					throw new RuntimeException("Inconsistency");
		}
	}
	
	public void gatherOtherObjects() {
		for (ObjectAccess oa : writeAccesses) 
			otherO.add(oa.o);
		
		for (ObjectAccess oa : otherAccesses) 
			otherO.add(oa.o);
	}
	
	@Override
	public String toString() {
		return toString("");
	}
	
	public String toString(String linePrefix) {
		StringBuffer s = new StringBuffer();
		s.append(linePrefix);
		s.append("Object : ");
		s.append(U.tos(o));
		if(this.otherO.size() != 1)
		{
			s.append("\nOther objects:");
			for (InstanceKey o : otherO) {
				s.append("\n");
				s.append(U.tos(o));
			}
		}
		return postfixToString(linePrefix, s);
	}


	protected String postfixToString(String linePrefix, StringBuffer s) {
		s.append("\n");
		s.append(linePrefix);
		s.append("   Write accesses:");
		for (ObjectAccess w : writeAccesses) {
			s.append("\n");
			s.append(linePrefix);
			s.append("     ");
			s.append(w.toString(U.detailedResults));
		}
		s.append("\n");
		s.append(linePrefix);
		s.append("   Other accesses:");
		for (ObjectAccess oa : otherAccesses) {
			s.append("\n");
			s.append(linePrefix);
			s.append("     ");
			s.append(oa.toString(U.detailedResults));
		}
		return s.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj.getClass() == ConcurrentAccess.class))
			return false;

		return this.compareTo((ConcurrentAccess) obj) == 0;
	}
	
	@Override
	public int compareTo(ConcurrentAccess other) {
		if(other instanceof ConcurrentFieldAccess)
			return 1;
		
		String string = o != null?o.toString():"";
		String string2 = other.o != null ? other.o.toString() : "";
		return ComparableTuple.from(t,string).compareTo(ComparableTuple.from(other.t, string2));
	}
	
	@Override
	public int hashCode() {
		return t.hashCode() + o.hashCode();
	}


	public boolean isEmpty() {
		return this.writeAccesses.isEmpty() || this.otherAccesses.isEmpty();
	}


	public int getNoPairs() {
		int w = this.writeAccesses.size();
		int o = this.otherAccesses.size();
		return w * o;
	}
}
