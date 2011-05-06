package sabazios.util;

import com.ibm.wala.fixpoint.AbstractVariable;
import com.ibm.wala.util.intset.IntSetUtil;
import com.ibm.wala.util.intset.MutableIntSet;

public class IntSetVariable extends AbstractVariable<IntSetVariable> {

	MutableIntSet intSet = IntSetUtil.getDefaultIntSetFactory().make();
	boolean isTop = false;

	public IntSetVariable() {
	}

	public IntSetVariable(boolean b) {
		isTop = b;
	}

	public boolean isTop() {
		return isTop;
	}

	@Override
	public void copyState(IntSetVariable v) {
		this.isTop = v.isTop;
		intSet.copySet(v.intSet);
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
}
