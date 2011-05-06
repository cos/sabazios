package sabazios.locksetCallGraph;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.fixpoint.IVariable;

public class LockIntersection extends AbstractMeetOperator<Lock> {

	public static final AbstractMeetOperator<Lock> instance = new LockIntersection();
	
	private LockIntersection() {
	}

	@Override
	public byte evaluate(Lock lhs, @SuppressWarnings("rawtypes") IVariable[] rhs) {
		Lock newLhs = (Lock) ((Lock) rhs[0]).clone();
		for(IVariable<?> v :rhs) {
			Lock other = (Lock) v;
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
		return "LOCK INTERSECTION";
	}
}
