package racefix;

import java.util.Iterator;

import sabazios.A;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.graph.traverse.BFSIterator;

public class AccessTrace {
	private final CGNode n;
	private final SSAInstruction i;
	private final A a;
	
	public AccessTrace(A a, CGNode n, SSAInstruction i) {
		this.a = a;
		this.n = n;
		this.i = i;
	}

	public void compute() {
		
	}
}