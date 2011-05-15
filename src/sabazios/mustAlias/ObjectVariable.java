package sabazios.mustAlias;

import java.util.Arrays;
import java.util.HashSet;

import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.types.FieldReference;

public class ObjectVariable extends HashSet<ObjectVariable.Representation> implements IVariable<ObjectVariable> {
	private static final long serialVersionUID = 645260992942405144L;

	// immutable
	public static class Representation {
		private static final long serialVersionUID = 1330578079848930804L;

		public final int v;
		public final FieldReference[] f;

		@SuppressWarnings("unchecked")
		public Representation(int v, FieldReference[] f) {
			this.v = v;
			this.f = f;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj.getClass() != this.getClass())
				return false;

			Representation other = (Representation) obj;
			return this.v == other.v && Arrays.equals(this.f, other.f);
		}

		@Override
		public int hashCode() {
			return this.v * 17 + this.f.length * 19;
		}
	}

	private int graphNodeId;
	private int orderNumber;

	@Override
	public void copyState(ObjectVariable other) {
		this.clear();
		for (Representation representation : other) {
			this.add((Representation) representation);
		}
	}

	@Override
	public int getGraphNodeId() {
		return this.graphNodeId;
	}

	@Override
	public void setGraphNodeId(int number) {
		this.graphNodeId = number;

	}

	@Override
	public int getOrderNumber() {
		return this.orderNumber;
	}

	@Override
	public void setOrderNumber(int i) {
		this.orderNumber = i;
	}

	public void put(int val, int ref, FieldReference declaredField) {
		HashSet<Representation> toAdd = new HashSet<Representation>();
		for (Representation r : this) {
			if (r.v == ref && r.f[0].equals(declaredField)) {
				this.remove(r);
				toAdd.add(new Representation(val, Arrays.copyOfRange(r.f, 1, r.f.length - 1)));
			}
		}
		this.addAll(toAdd);
	}

	public void get(int def, int ref, FieldReference declaredField) {
		
		
	}
}
