package sabazios.mustAlias;

import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ssa.SSAPutInstruction;

public class PutTF extends UnaryOperator<ObjectVariable> {

	private final SSAPutInstruction i;

	public PutTF(SSAPutInstruction instruction) {
		this.i = instruction;
	}

	@Override
	public byte evaluate(ObjectVariable lhs, ObjectVariable rhs) {
		Object old_lhs = lhs.clone();
		lhs.put(i.getVal(), i.getRef(), i.getDeclaredField());
		
			
		if(lhs.equals(old_lhs))
			return NOT_CHANGED;
		else
			return CHANGED;
	}

	@Override
	public int hashCode() {
		return 41;
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public String toString() {
		return "Put TF";
	}

}
