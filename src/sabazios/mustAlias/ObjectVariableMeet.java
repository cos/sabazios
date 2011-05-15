package sabazios.mustAlias;

import java.util.HashSet;

import sabazios.mustAlias.ObjectVariable.Representation;
import sabazios.util.Relation;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.fixpoint.IVariable;

public class ObjectVariableMeet extends AbstractMeetOperator<ObjectVariable> {
	
	public static final ObjectVariableMeet instance = new ObjectVariableMeet();

	@SuppressWarnings("rawtypes")
	@Override
	public byte evaluate(ObjectVariable lhs, IVariable[] rhs) {
		ObjectVariable old_lhs = (ObjectVariable) lhs.clone();
		for (IVariable<?> v : rhs) {
			ObjectVariable other = (ObjectVariable) v;
			lhs.addAll(other);
		}
		
		return lhs.equals(old_lhs) ? NOT_CHANGED : CHANGED;
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
		return "ObjectVariable MEET";
	}

}
