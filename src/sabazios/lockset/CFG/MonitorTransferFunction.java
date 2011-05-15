package sabazios.lockset.CFG;

import sabazios.util.IntSetVariable;

import com.ibm.wala.fixpoint.UnaryOperator;

class MonitorTransferFunction extends UnaryOperator<IntSetVariable> {
	private final int ref;

	final static MonitorTransferFunction[] monitor = new MonitorTransferFunction[1000];

	public static MonitorTransferFunction get(int ref) {
		if (monitor[ref] != null)
			return monitor[ref];
		else {
			monitor[ref] = new MonitorTransferFunction(ref);
			return monitor[ref];
		}
	}

	private MonitorTransferFunction(int ref) {
		this.ref = ref;
	}

	@Override
	public byte evaluate(IntSetVariable lhs, IntSetVariable rhs) {
		if (lhs.contains(ref))
			return NOT_CHANGED;
		else {
			lhs.add(ref);
			return CHANGED;
		}
	}

	@Override
	public int hashCode() {
		return ref;
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public String toString() {
		return " monitor: " + ref;
	}

}