package sabazios;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import sabazios.domains.Loop;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.ObjectAccess;
import sabazios.domains.WriteFieldAccess;
import sabazios.locksetCallGraph.Lock;
import sabazios.util.U;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.graph.GraphPrint;

import edu.illinois.reLooper.sabazios.CodeLocation;
import edu.illinois.reLooper.sabazios.log.Log;

public class RaceAnalysis {

	public final CallGraph callGraph;
	final PointerAnalysis pointerAnalysis;
	public HeapGraph heapGraph;
	final PropagationCallGraphBuilder builder;
	public IClassHierarchy cha;
	public Locks locks;

	public RaceAnalysis(CallGraph callGraph, PointerAnalysis pointerAnalysis, PropagationCallGraphBuilder builder) {
		this.callGraph = callGraph;
		this.pointerAnalysis = pointerAnalysis;
		this.builder = builder;
		this.heapGraph = this.pointerAnalysis.getHeapGraph();
		this.cha = this.pointerAnalysis.getClassHierarchy();

		this.pointerForValue = new PointerForValue(this);
		this.w = new WriteAccesses(this);
		this.o = new OtherAccesses(this);
		this.t = new AbstractThreads();
		this.cfa = new ConcurrentFieldAccesses(this);
		this.locks = new Locks(callGraph);
	}

	public PointerForValue pointerForValue;
	public WriteAccesses w;
	public OtherAccesses o;
	public AbstractThreads t;
	public ConcurrentFieldAccesses cfa; // concurrent field accesses
	public ConcurrentFieldAccesses ucfa; // unprotected concurrent field
											// accesses
	public ConcurrentAccesses ca; // final list of concurrent accesses

	public void compute() {
		pointerForValue.compute();
		Log.log("Function CGNode x SSAvalue -> Object precomputed");

		w.compute();
		Log.log("Found all relevant writes: "+w.size());
		o.compute();
		Log.log("Found all relevant other accesses: "+o.size());
		cfa.compute();

		System.out.println("---- Conccurent accesses ---- ");
//		System.out.println(cfa.toString());
		System.out.println();
		Log.log("Concurrent instructions done. # = "+cfa.getNoPairs());

		locks.compute();

//		for (CGNode n : locks.locksForCGNodes.keySet()) {
//			Lock lock = locks.locksForCGNodes.get(n);
//			if (!lock.isEmpty())
//				System.out.println(n + " -- " + lock);
//		}

		Log.log("Computed locks");

		filter();

		System.out.println("---- Filtered conccurent accesses ---- ");
//		System.out.println(ucfa.toString());
		System.out.println();
		Log.log("Deep races done. # = "+ucfa.getNoPairs());

		ca = ucfa.rippleUp();
		Log.log("Rippleup up races in libraries");

		ca.reduceNonConcurrentAndSimilarLooking();
		System.out.println("---- Shallow conccurent accesses ---- ");
		System.out.println(ca.toString());
		Log.log("Deep races done. # = "+ca.getNoPairs());
		
		System.out.println("------- Other stuff ----------");
		System.out.println(this.callGraph.getNumberOfNodes());
		
	}

	private void debugPrintMethod(String regex) {
		LocalPointerKey localPointerKey;
		Set<CGNode> nodes = findNodes(regex);
		for (CGNode cgNode : nodes) {
			System.out.println("_+_+_+_+_+_+_");
			System.out.println(cgNode);
			for (int i = 1; i < 10; i++) {
				localPointerKey = pointerForValue.get(cgNode, i);
				System.out.print(i);
				System.out.println(localPointerKey == null ? " Primitive" : " Pointer ");
				if (localPointerKey == null)
					continue;
				SSAInstruction[] instructions = cgNode.getIR().getControlFlowGraph().getInstructions();
				String variableName = CodeLocation.variableName(i, cgNode, instructions[instructions.length - 1]);
				System.out.print(variableName);
				;
				Iterator<Object> succNodes = heapGraph.getSuccNodes(localPointerKey);
				while (succNodes.hasNext()) {
					InstanceKey object = (InstanceKey) succNodes.next();
					System.out.println(" -> " + U.tos(object));
					if (variableName != null && variableName.equals("hTable")) {
						Iterator<Object> succNodes11 = heapGraph.getSuccNodes(object);
						while (succNodes11.hasNext()) {
							Object object2 = (Object) succNodes11.next();
							Iterator<Object> succNodes2 = heapGraph.getSuccNodes(object2);
							while (succNodes2.hasNext()) {
								InstanceKey object3 = (InstanceKey) succNodes2.next();
								System.out.println(" -> -> " + object3);
							}
						}
					}
				}
			}
			System.out.println("---- suc nodes ---- ");
			Iterator<CGNode> succNodes = callGraph.getSuccNodes(cgNode);
			while (succNodes.hasNext()) {
				CGNode cgNode1 = (CGNode) succNodes.next();
				System.out.println(cgNode1);
			}
		}
	}

	private Set<CGNode> findNodes(String regex) {
		Iterator<CGNode> iterator = callGraph.iterator();
		HashSet<CGNode> nodes = new HashSet<CGNode>();
		while (iterator.hasNext()) {
			CGNode n = iterator.next();
			if (n.getMethod().toString().matches(regex)) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	private CGNode findNode(String regex) {
		return findNodes(regex).iterator().next();
	}

	private void filter() {
		this.ucfa = new ConcurrentFieldAccesses(this);
		for (Loop t : this.cfa.accesses.keySet()) {
			TreeSet<ConcurrentAccess> newAccesses = new TreeSet<ConcurrentAccess>();
			for (ConcurrentAccess ca : this.cfa.accesses.get(t)) {
				ConcurrentFieldAccess cfa = (ConcurrentFieldAccess) ca;
				ConcurrentFieldAccess newCA = new ConcurrentFieldAccess(t, cfa.o, cfa.f);
				for (ObjectAccess oa : cfa.writeAccesses) {
					WriteFieldAccess w = (WriteFieldAccess) oa;
					Lock l = locks.get(w.n, w.i);
					if (!l.containsNode(w.n) || (!l.get(w.n).contains(1)))
						newCA.writeAccesses.add(w);
				}

				for (ObjectAccess oa : ca.otherAccesses) {
					FieldAccess w = (FieldAccess) oa;
					Lock l = locks.get(w.n, w.i);
					if (!l.containsNode(w.n) || (!l.get(w.n).contains(1)))
						newCA.otherAccesses.add(w);
				}
				if (!newCA.isEmpty())
					newAccesses.add(newCA);
			}
			if (!newAccesses.isEmpty())
				ucfa.accesses.put(t, newAccesses);
		}
	}

	public ConcurrentAccesses getRaces() {
		return ca;
	}

	public Object traceBackToShared(CGNode predNode, int use) {
		// TODO Auto-generated method stub
		return null;
	}
}
