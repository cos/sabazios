package sabazios.domains;

import sabazios.util.U;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

import edu.illinois.reLooper.sabazios.CodeLocation;

public class Loop implements Comparable<Loop> {
	public final InstanceKey array;
	public final CGNode operatorCaller;
	public final CallSiteReference operatorCallSite;

	public Loop(InstanceKey array, CGNode operatorCaller, CallSiteReference operatorCallSite) {
		this.array = array;
		this.operatorCaller = operatorCaller;
		this.operatorCallSite = operatorCallSite;
	}

	@Override
	public int hashCode() {
		return array.hashCode() + operatorCaller.hashCode() + operatorCallSite.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Loop))
			return false;
		Loop other = (Loop) obj;
		return array.equals(other.array) && operatorCaller.equals(other.operatorCaller)
				&& operatorCallSite.equals(other.operatorCallSite);
	}

	@Override
	public String toString() {
		if (U.detailedResults)
			return CodeLocation.make(operatorCaller, operatorCaller.getIR().getCalls(operatorCallSite)[0]) .toString() + " Array: " + U.tos(array);
		else
			return CodeLocation.make(operatorCaller, operatorCaller.getIR().getCalls(operatorCallSite)[0]).toString();
	}

	@Override
	public int compareTo(Loop o) {
		return this.toString().compareTo(o.toString());
	}
}
