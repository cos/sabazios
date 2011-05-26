package sabazios.lockset.callGraph;

import com.ibm.wala.fixpoint.UnaryOperator;

public class LockIdentity extends UnaryOperator<LockSetVariable> {

	public static final UnaryOperator<LockSetVariable> instance = new LockIdentity();

	@Override
	public byte evaluate(LockSetVariable lhs, LockSetVariable rhs) {
		if(lhs.equals(rhs))
			return NOT_CHANGED;
		else {
			lhs.copyState(rhs);
			return CHANGED;
		}
	}

	@Override
	public int hashCode() {
		return 37;
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public String toString() {
		return "Lock identity";
	}

}
