package sabazios.util;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.fixpoint.IVariable;

public class IntSetVariableUnion extends AbstractMeetOperator<IntSetVariable> {
	
	public final static IntSetVariableUnion instance = new IntSetVariableUnion();

	private IntSetVariableUnion() {
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
		return "UNION";
	}

	@SuppressWarnings({"rawtypes" })
	@Override
	public byte evaluate(IntSetVariable lhs, IntSetVariable[] rhs) {
		if (lhs == null) {
			throw new IllegalArgumentException("null lhs");
		}
		if (rhs == null) {
			throw new IllegalArgumentException("rhs == null");
		}
		IntSetVariable newV = (IntSetVariable) ((IntSetVariable)rhs[0]).clone();
		for (IVariable v : rhs) {
			newV.union((IntSetVariable)v);
		}
		if (!lhs.equals(newV)) {
			lhs.copyState(newV);
			return CHANGED;
		} else
			return NOT_CHANGED;
	}

}
