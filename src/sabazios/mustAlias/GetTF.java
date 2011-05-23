package sabazios.mustAlias;

import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ssa.SSAGetInstruction;

public class GetTF extends UnaryOperator<ObjectVariable> {

	private final SSAGetInstruction i;

	public GetTF(SSAGetInstruction instruction) {
		this.i = instruction;
	}

	@Override
	public byte evaluate(ObjectVariable lhs, ObjectVariable rhs) {
		Object old_lhs = lhs.clone();
		lhs.get(i.getDef(), i.getRef(), i.getDeclaredField());
		
			
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
