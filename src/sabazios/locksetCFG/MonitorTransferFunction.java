package sabazios.locksetCFG;

import sabazios.util.IntSetVariable;

import com.ibm.wala.fixpoint.UnaryOperator;

class MonitorTransferFunction extends UnaryOperator<IntSetVariable> {
	private final int ref;
	private final boolean monitorEnter;

	final static MonitorTransferFunction[] enterMonitor = new MonitorTransferFunction[1000];
	final static MonitorTransferFunction[] exitMonitor = new MonitorTransferFunction[1000];

	public static MonitorTransferFunction get(int ref, boolean monitorEnter) {
		if (monitorEnter)
			if (enterMonitor[ref] != null)
				return enterMonitor[ref];
			else {
				enterMonitor[ref] = new MonitorTransferFunction(ref, monitorEnter);
				return enterMonitor[ref];
			}
		else if (exitMonitor[ref] != null)
			return exitMonitor[ref];
		else {
			exitMonitor[ref] = new MonitorTransferFunction(ref, monitorEnter);
			return exitMonitor[ref];
		}
	}

	private MonitorTransferFunction(int ref, boolean monitorEnter) {
		this.ref = ref;
		this.monitorEnter = monitorEnter;
	}

	@Override
	public byte evaluate(IntSetVariable lhs, IntSetVariable rhs) {
		if (monitorEnter) {
			if (lhs.contains(ref))
				return NOT_CHANGED;
			else {
				lhs.add(ref);
				return CHANGED;
			}
		} else {
			if (lhs.contains(ref)) {
				lhs.remove(ref);
				return CHANGED;
			} else
				return NOT_CHANGED;
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
		return (monitorEnter ? "Enter" : "Exit") + " monitor: " + ref;
	}

}