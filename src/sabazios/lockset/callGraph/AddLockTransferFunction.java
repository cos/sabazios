package sabazios.lockset.callGraph;

import sabazios.util.IntSetVariable;

import com.ibm.wala.fixpoint.UnaryOperator;
import com.ibm.wala.ipa.callgraph.CGNode;

public class AddLockTransferFunction extends UnaryOperator<Lock> {
	private final CGNode src;
	private final IntSetVariable var;

	public AddLockTransferFunction(CGNode src, IntSetVariable var) {
		this.src = src;
		this.var = var;
	}

	@Override
	public byte evaluate(Lock lhs, Lock rhs) {
		Lock new_lhs = (Lock) rhs.clone();
		
		new_lhs.addNewVars(src, var);
		
		if(new_lhs.equals(lhs))
			return NOT_CHANGED;
		else {
			lhs.copyState(new_lhs);
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
		return "Add locks for node "+src+" variables "+var;
	}

}