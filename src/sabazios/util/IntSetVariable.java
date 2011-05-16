package sabazios.util;

import java.util.Iterator;

import com.ibm.wala.fixpoint.AbstractVariable;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSetUtil;
import com.ibm.wala.util.intset.MutableIntSet;

public class IntSetVariable extends AbstractVariable<IntSetVariable> implements Iterable<Integer> {

	MutableIntSet intSet = IntSetUtil.getDefaultIntSetFactory().make();
	boolean isTop = false;

	public IntSetVariable() {
	}

	private IntSetVariable(boolean b) {
		isTop = b;
	}
	
	public static IntSetVariable newTop() {
		return new IntSetVariable(true);
	}

	public boolean isTop() {
		return isTop;
	}

	@Override
	public void copyState(IntSetVariable v) {
		this.isTop = v.isTop;
		this.intSet = IntSetUtil.getDefaultIntSetFactory().makeCopy(v.intSet);
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != this.getClass())
			return false;
		IntSetVariable other = (IntSetVariable) obj;
		return isTop ? other.isTop : (!other.isTop && this.intSet.sameValue(other.intSet));
	}

	public IntSetVariable clone() {
		IntSetVariable newV = new IntSetVariable();
		newV.copyState(this);
		return newV;
	}

	public void intersect(IntSetVariable v) {
		if (this.isTop) {
			this.copyState(v);
			return;
		}
		if (v.isTop)
			return;

		this.isTop = false;
		this.intSet.intersectWith(v.intSet);
	}

	public boolean contains(int ref) {
		if(isTop)
			return true;
		return intSet.contains(ref);
	}

	public void add(int ref) {
		intSet.add(ref);
	}

	public void remove(int ref) {
		if(isTop)
			throw new RuntimeException("Don't allow this.");
		intSet.remove(ref);
	}

	public boolean isEmpty() {
		if(isTop)
			return false;
		return intSet.isEmpty();
	}

	public void union(IntSetVariable var) {
		if(var.isTop)
			this.isTop = true;
		else
			intSet.addAll(var.intSet);
	}
	@Override
	public String toString() {
		if(isTop)
			return "TOP";
		else
			return intSet.toString();
	}

	public IntIterator intIterator() {
		return intSet.intIterator();
	}

	public void diff(IntSetVariable other) {
		IntIterator intIterator = other.intIterator();
		while(intIterator.hasNext()) 
			intSet.remove(intIterator.next());
	}

	@Override
	public Iterator<Integer> iterator() {
		return new IntSetIterator(this.intSet.intIterator());
	}
	
	private final class IntSetIterator implements Iterator<Integer> {
		private final IntIterator intIterator;

		public IntSetIterator(IntIterator intIterator) {
			this.intIterator = intIterator;
		}

		@Override
		public boolean hasNext() {
			return intIterator.hasNext();
		}

		@Override
		public Integer next() {
			return intIterator.next();
		}

		@Override
		public void remove() {
			throw new RuntimeException("NOT IMPLEMENTED");
		}
	}
}
