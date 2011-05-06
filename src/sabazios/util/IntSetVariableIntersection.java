package sabazios.util;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.fixpoint.IVariable;

public class IntSetVariableIntersection extends AbstractMeetOperator<IntSetVariable> {
	
	public final static IntSetVariableIntersection instance = new IntSetVariableIntersection();

	private IntSetVariableIntersection() {
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
		return "INTERSECTION";
	}

	@SuppressWarnings({"rawtypes" })
	@Override
	public byte evaluate(IntSetVariable lhs, IVariable[] rhs) {
		if (lhs == null) {
			throw new IllegalArgumentException("null lhs");
		}
		if (rhs == null) {
			throw new IllegalArgumentException("rhs == null");
		}
		IntSetVariable newV = (IntSetVariable) ((IntSetVariable)rhs[0]).clone();
		for (IVariable v : rhs) {
			newV.intersect((IntSetVariable)v);
		}
		if (!lhs.equals(newV)) {
			lhs.copyState(newV);
			return CHANGED;
		} else
			return NOT_CHANGED;
	}

}
