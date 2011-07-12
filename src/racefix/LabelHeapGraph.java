package racefix;

import java.util.Iterator;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.graph.traverse.BFSIterator;

public class LabelHeapGraph {
	private final HeapGraph heapGraph;
	public LabelHeapGraph(HeapGraph heapGraph) {
		this.heapGraph = heapGraph;
	}
	public void compute() {
		BFSIterator<?> bfs = new BFSIterator<Object>(heapGraph);
		for(Object o: heapGraph) {
			if(o instanceof AllocationSiteInNode) {
				AllocationSiteInNode site = (AllocationSiteInNode) o;
			}
		}
	}
}