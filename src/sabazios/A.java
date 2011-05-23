package sabazios;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.domains.ConcurrentAccess;
import sabazios.domains.Loop;
import sabazios.domains.ObjectAccess;
import sabazios.domains.WriteFieldAccess;
import sabazios.lockIdentity.Dereferences;
import sabazios.lockIdentity.Dereferences.Deref;
import sabazios.lockIdentity.Dereferences.DerefDeque;
import sabazios.lockset.callGraph.Lock;
import sabazios.lockset.callGraph.Lock.Individual;
import sabazios.util.FlexibleContext;
import sabazios.util.Log;
import sabazios.util.wala.viz.DotUtil;
import sabazios.util.wala.viz.NodeDecorator;
import sabazios.util.wala.viz.PDFViewUtil;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.warnings.WalaException;

public class A {

	public static CallGraph callGraph;
	public static PointerAnalysis pointerAnalysis;
	public static HeapGraph heapGraph;
	public static PropagationCallGraphBuilder builder;
	public static IClassHierarchy cha;
	public static Locks locks;
	public static PointerForValue pointerForValue;

	public A(CallGraph callGraph, PointerAnalysis pointerAnalysis, PropagationCallGraphBuilder builder) {
		A.callGraph = callGraph;
		A.pointerAnalysis = pointerAnalysis;
		A.builder = builder;
		A.heapGraph = A.pointerAnalysis.getHeapGraph();
		A.cha = A.pointerAnalysis.getClassHierarchy();

		A.pointerForValue = new PointerForValue(this);
		A.w = new WriteAccesses();
		A.o = new OtherAccesses();
		A.t = new AbstractThreads();
		this.initialRaces = new ConcurrentFieldAccesses();
		A.locks = new Locks(callGraph);
	}

	public static WriteAccesses w;
	public static OtherAccesses o;
	public static AbstractThreads t;
	public ConcurrentFieldAccesses initialRaces; // concurrent field accesses
	public ConcurrentFieldAccesses deepRaces; // unprotected concurrent field
												// accesses
	public ConcurrentAccesses shallowRaces; // final list of concurrent accesses

	public void compute() {
		PDFViewUtil.raceAnalysis = this;
		// Graph<Integer> prunedM = GraphSlicer.prune(m, new
		// Filter<Integer>() {
		// @Override
		// public boolean accepts(Integer o) {
		// return m.getSuccNodeCount(o) > 0 || m.getPredNodeCount(o) > 0;
		// }
		// });

		System.out.println("---- Compute map value -> PointerKey ------------------- ");
		pointerForValue.compute();
		Log.log("Function CGNode x SSAvalue -> Object precomputed");
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Reads and writes ---------------------------------- ");
		w.compute();
		Log.log("Found all relevant writes: " + w.size());
		o.compute();
		Log.log("Found all relevant other accesses: " + o.size());
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Compute locks ------------------------------------- ");
		locks.compute();
		for (CGNode n : locks.locksForCGNodes.keySet()) {
			Lock lock = locks.locksForCGNodes.get(n);
			if (!lock.isEmpty())
				System.out.println(n + " -- " + lock);
		}
		// dotCallGraph(); decoreator to use: new CGNodeDecorator(this)
		// dotIRFor(".*op().*");
		Log.log("Computed locks");
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Initial races ------------------------------------- ");
		initialRaces.compute();
		System.out.println(initialRaces.toString());
		System.out.println();
		// Distribute locks
		initialRaces.distributeLocks();
		Log.log("Initial races. # = " + initialRaces.getNoPairs());
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Deep races ---------------------------------------- ");
		// Lock identity
		// LockIdentity li = new LockIdentity(this);
		// li.compute();
		filter(initialRaces);
		this.deepRaces = this.initialRaces;
		System.out.println(deepRaces.toString());
		System.out.println();
		Log.log("Deep races done. # = " + deepRaces.getNoPairs());
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Shallow races ------------------------------------- ");
		// Bubble up races
		shallowRaces = deepRaces.rippleUp();
		Log.log("Rippleup up races in libraries");
		shallowRaces.reduceNonConcurrentAndSimilarLooking();
		System.out.println("---- Shallow races ---- ");
		System.out.println(shallowRaces.toString());
		Log.log("Shallow races done. # = " + shallowRaces.getNoPairs());
		System.out.println("-------------------------------------------------------- \n");

		// Other stuff
		System.out.println("------- Other stuff ------------------------------------ ");
		System.out.println(A.callGraph.getNumberOfNodes());
		System.out.println("-------------------------------------------------------- \n");
	}

	private void filter(ConcurrentFieldAccesses races) {
		for (Loop l : races.keySet()) {
			Iterator<ConcurrentAccess> iterca = races.get(l).iterator();
			while (iterca.hasNext()) {
				ConcurrentAccess ca = (ConcurrentAccess) iterca.next();				
				for (ObjectAccess oa1 : ca.writeAccesses) {
					Lock lock1 = locks.get(oa1.n, oa1.i);
					Iterator<ObjectAccess> iter = ca.otherAccesses.iterator();
					while (iter.hasNext()) {
						ObjectAccess oa2 = (ObjectAccess) iter.next();
						Lock lock2 = locks.get(oa2.n, oa2.i);
						if (validProtection(lock1, lock2))
							iter.remove();
					}
					if(ca.otherAccesses.size() == 0)
						iterca.remove();
				}
			}
		}
	}

	private boolean validProtection(Lock lock1, Lock lock2) {
		Set<Individual> locks1 = lock1.getIndividualLocks();
		LinkedHashSet<ArrayDeque<Deref>> lockReps1 = new LinkedHashSet<ArrayDeque<Deref>>();
		for (Individual ilock : locks1) {
			Set<DerefDeque> infer = Dereferences.get(ilock.n, ilock.v);
			if (infer.size() == 1)
				lockReps1.add(infer.iterator().next());
		}

		Set<Individual> locks2 = lock2.getIndividualLocks();
		LinkedHashSet<ArrayDeque<Deref>> lockReps2 = new LinkedHashSet<ArrayDeque<Deref>>();
		for (Individual ilock : locks2) {
			Set<DerefDeque> infer = Dereferences.get(ilock.n, ilock.v);
			if (infer.size() == 1)
				lockReps2.add(infer.iterator().next());
		}

		for (ArrayDeque<Deref> arrayDeque1 : lockReps1) {
			for (ArrayDeque<Deref> arrayDeque2 : lockReps2) {
				if (same(arrayDeque1, arrayDeque2))
					return true;
			}
		}
		return false;
	}

	private boolean same(ArrayDeque<Deref> l1, ArrayDeque<Deref> l2) {
		if(l1.size() != l2.size())
			return false;
		if(!l1.peek().n.equals(l2.peek().n) || l1.peek().v != l2.peek().v)
			return false;
		Iterator<Deref> it1 = l1.iterator();
		Iterator<Deref> it2 = l2.iterator();
		while(it1.hasNext()) {
			Deref d1 = it1.next();
			Deref d2 = it2.next();
			if(d1.f == null)
				break;
			if(!d1.f.equals(d2.f)) 
				return false;
		}
		
		return noWritesToOurBelovedObjects(l1) && noWritesToOurBelovedObjects(l2);
	}

	private boolean noWritesToOurBelovedObjects(ArrayDeque<Deref> l1) {
		FlexibleContext context = (FlexibleContext)l1.peek().n.getContext();
		CGNode cgn = (CGNode) context.getItem(CS.OPERATOR_CALLER);
		for (Deref d : l1) {
			LocalPointerKey pk = A.pointerForValue.get(d.n, d.v);
			Iterator<Object> iks = A.heapGraph.getSuccNodes(pk);
			while (iks.hasNext()) {
				InstanceKey ik = (InstanceKey) iks.next();
				for (Loop loop : this.w.keySet()) {
					if (loop.operatorCaller.equals(cgn)) {
						HashMap<InstanceKey, HashSet<WriteFieldAccess>> hashMap = this.w.get(loop);
						if(hashMap.containsKey(ik))
						{
							HashSet<WriteFieldAccess> hashSet = hashMap.get(ik);
							for (WriteFieldAccess wf : hashSet) {
								if(wf.f.getReference().equals(d.f))
									return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unused")
	private void dotIRFor(String s) {
		for (CGNode cgNode : callGraph) {
			if (cgNode.getMethod().toString().matches(s))
				try {
					System.out.println("Generated: " + "debug/irPdf" + cgNode.getGraphNodeId());

					PDFViewUtil.ghostviewIR(cha, cgNode, "debug/irPdf" + cgNode.getGraphNodeId(), "./debug/irDot"
							+ cgNode.getGraphNodeId(), "/opt/local/bin/dot", "open");
				} catch (WalaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public static void dotGraph(Graph<?> m, String name, NodeDecorator decorator) {
		String dotFile = "debug/" + name + ".dot";
		String outputFile = "./debug/" + name + ".pdf";
		try {
			DotUtil.dotify(m, decorator, dotFile, outputFile, "/opt/local/bin/dot");
			PDFViewUtil.launchPDFView(outputFile, "open");
		} catch (WalaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
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
