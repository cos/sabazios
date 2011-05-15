package sabazios;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.Loop;
import sabazios.domains.ObjectAccess;
import sabazios.domains.WriteFieldAccess;
import sabazios.lockset.callGraph.Lock;
import sabazios.util.CodeLocation;
import sabazios.util.IntSetVariable;
import sabazios.util.Log;
import sabazios.util.U;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.intset.IntIterator;


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
		this.initialRaces = new ConcurrentFieldAccesses(this);
		this.locks = new Locks(callGraph);
	}

	public PointerForValue pointerForValue;
	public WriteAccesses w;
	public OtherAccesses o;
	public AbstractThreads t;
	public ConcurrentFieldAccesses initialRaces; // concurrent field accesses
	public ConcurrentFieldAccesses deepRaces; // unprotected concurrent field
											// accesses
	public ConcurrentAccesses shallowRaces; // final list of concurrent accesses

	public void compute() {
		
		pointerForValue.compute();
		Log.log("Function CGNode x SSAvalue -> Object precomputed");

		w.compute();
		Log.log("Found all relevant writes: "+w.size());
		o.compute();
		Log.log("Found all relevant other accesses: "+o.size());
		initialRaces.compute();

		System.out.println("---- Initial races ---- ");
		System.out.println(initialRaces.toString());
		System.out.println();
		Log.log("Initial races. # = "+initialRaces.getNoPairs());

		locks.compute();

		for (CGNode n : locks.locksForCGNodes.keySet()) {
			Lock lock = locks.locksForCGNodes.get(n);
			if (!lock.isEmpty())
				System.out.println(n + " -- " + lock);
		}

		Log.log("Computed locks");
		
		initialRaces.distributeLocks();
		

//		LockIdentity li = new LockIdentity(this);
//		li.compute();
		this.deepRaces = this.initialRaces;

		System.out.println("---- Races (after very very simple filter) ---- ");
		System.out.println(deepRaces.toString());
		System.out.println();
		Log.log("Deep races done. # = "+deepRaces.getNoPairs());

		shallowRaces = deepRaces.rippleUp();
		Log.log("Rippleup up races in libraries");

		shallowRaces.reduceNonConcurrentAndSimilarLooking();
		System.out.println("---- Shallow races ---- ");
		System.out.println(shallowRaces.toString());
		Log.log("Shallow races done. # = "+shallowRaces.getNoPairs());
		
		System.out.println("------- Other stuff ----------");
		System.out.println(this.callGraph.getNumberOfNodes());
		
	}

	@SuppressWarnings("unused")
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

	public ConcurrentAccesses getRaces() {
		return shallowRaces;
	}

	public Object traceBackToShared(CGNode predNode, int use) {
		// TODO Auto-generated method stub
		return null;
	}
}
