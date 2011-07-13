package racefix;

import java.util.Iterator;
import java.util.LinkedHashSet;

import sabazios.A;
import sabazios.domains.PointerForValue;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.graph.traverse.BFSIterator;

public class AccessTrace {
	private final CGNode n;
	private final int v;
	private final A a;
	private LinkedHashSet<PointerKey> pointers = new LinkedHashSet<PointerKey>();
	private LinkedHashSet<InstanceKey> instances = new LinkedHashSet<InstanceKey>();
	private PointerForValue pv;

	public AccessTrace(A a, CGNode n, int v) {
		this.a = a;
		this.n = n;
		this.v = v;
		pv = a.pointerForValue;
	}

	public void compute() {
		LocalPointerKey lpk = pv.get(n, v);
		System.out.println(lpk);
		pointers.add(lpk);
		Iterator<Object> succNodes = a.heapGraph.getSuccNodes(lpk);
		while (succNodes.hasNext()) {
			InstanceKey o = (InstanceKey) succNodes.next();
			instances.add(o);
		}

	}

	public String getTestString() {
		String s = "";
//		for (InstanceKey o : instances) {
//			if (o instanceof AllocationSiteInNode) {
//				AllocationSiteInNode site = (AllocationSiteInNode) o;
//				s += "";
//			}
//		}
		return instances.toString() + "\n\n" + pointers.toString();
	}
}