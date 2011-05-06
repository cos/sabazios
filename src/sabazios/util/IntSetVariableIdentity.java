package sabazios.util;

import com.ibm.wala.fixpoint.UnaryOperator;

public class IntSetVariableIdentity extends UnaryOperator<IntSetVariable> {
	
	public final static IntSetVariableIdentity instance =  new IntSetVariableIdentity();
	
	private IntSetVariableIdentity() {
		
	}

	@Override
	public String toString() {
		return "IDENTITY";
	}

	@Override
	public int hashCode() {
		return 127;
	}

	@Override
	public boolean equals(Object o) {
		return true;
	}

	@Override
	public byte evaluate(IntSetVariable lhs, IntSetVariable rhs) {
		if (lhs == null) {
			throw new IllegalArgumentException("lhs cannot be null");
		}

		if (lhs.equals(rhs)) {
			return NOT_CHANGED;
		} else {
			lhs.copyState(rhs);
			return CHANGED;
		}
	}
	
	@Override
	public boolean isIdentity() {
		return true;
	}
};
