package sabazios.lockset.callGraph;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.fixpoint.IVariable;

public class LockMeet extends AbstractMeetOperator<LockSetVariable> {

	public static final AbstractMeetOperator<LockSetVariable> instance = new LockMeet();
	
	public static boolean firstPass = true;
	
	private LockMeet() {
	}

	@Override
	public byte evaluate(LockSetVariable lhs, LockSetVariable[] rhs) {
		LockSetVariable newLhs = (LockSetVariable) rhs[0].clone();
		for(IVariable<?> v :rhs) {
			LockSetVariable other = (LockSetVariable) v;
			if(firstPass)
				newLhs.unite(other);
			else
				newLhs.intersect(other);
		}
		if(newLhs.equals(lhs))
			return NOT_CHANGED;
		else {
			lhs.copyState(newLhs);
			return CHANGED;
		}
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object o) {
		return this==o;
	}

	@Override
	public String toString() {
		return "LOCK MEET";
	}
}
