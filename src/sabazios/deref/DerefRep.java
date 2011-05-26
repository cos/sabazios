package sabazios.deref;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;

public class DerefRep extends ArrayDeque<Deref> {
	private static final long serialVersionUID = 5575770507179190896L;

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj.getClass() != this.getClass())
			return false;
		
		DerefRep other = (DerefRep) obj;
		return Arrays.equals(this.toArray(), other.toArray());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.toArray());
	}
	
	public boolean same(DerefRep other) {
		if(other == null)
			return false;
		if(this.size() != other.size())
			return false;
		Deref d1 = this.peek();
		Deref d2 = other.peek();
		
		if(!((d1.v == 0 && d2.v == 0) || (Dereferences.isStopNode(d1.n) && Dereferences.isStopNode(d2.n) && d1.v == 1 && d2.v ==1)))
			return false;
		
		Iterator<Deref> it1 = this.iterator();
		Iterator<Deref> it2 = other.iterator();
		while(it1.hasNext()) {
			 d1 = it1.next();
			 d2 = it2.next();
			if(d1.f == null)
				break;
			if(!d1.f.equals(d2.f)) 
				return false;
		}
		return true;
	}
}