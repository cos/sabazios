package sabazios;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentAccesses;
import sabazios.domains.ConcurrentFieldAccesses;
import sabazios.domains.ConcurrentShallowAccesses;
import sabazios.domains.FilterSafe;
import sabazios.domains.Loops;
import sabazios.domains.OtherAccesses;
import sabazios.domains.PointerForValue;
import sabazios.domains.WriteAccesses;
import sabazios.lockset.Lock;
import sabazios.lockset.Locks;
import sabazios.util.Log;
import sabazios.util.wala.viz.DotUtil;
import sabazios.util.wala.viz.NodeDecorator;
import sabazios.util.wala.viz.PDFViewUtil;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
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

	public static void init(CallGraph callGraph, PointerAnalysis pointerAnalysis, PropagationCallGraphBuilder builder) {
		A.callGraph = callGraph;
		A.pointerAnalysis = pointerAnalysis;
		A.builder = builder;
		A.heapGraph = A.pointerAnalysis.getHeapGraph();
		A.cha = A.pointerAnalysis.getClassHierarchy();

		A.pointerForValue = new PointerForValue();
		A.write = new WriteAccesses();
		A.o = new OtherAccesses();
		A.loops = new Loops();
		A.locks = new Locks(callGraph);
	}

	public static WriteAccesses write;
	public static OtherAccesses o;
	public static Loops loops;
	


	public static ConcurrentAccesses compute() {
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
		write.compute();
		Log.log("Found all relevant writes: " + write.size());
		o.compute();
		Log.log("Found all relevant other accesses: " + o.size());
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Compute locks ------------------------------------- ");
		locks.compute();
		for (CGNode n : locks.locksForCGNodes.keySet()) {
			Set<Lock> lock = locks.locksForCGNodes.get(n);
			if (!lock.isEmpty())
				System.out.println(n + " -- " + lock);
		}
		// dotCallGraph(); decoreator to use: new CGNodeDecorator(this)
		// dotIRFor(".*op().*");
		Log.log("Computed locks");
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Initial races ------------------------------------- ");
		ConcurrentFieldAccesses initialRaces = new ConcurrentFieldAccesses();
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
		FilterSafe.filter(initialRaces);
		ConcurrentFieldAccesses deepRaces = initialRaces;
		System.out.println(deepRaces.toString());
		System.out.println();
		Log.log("Deep races done. # = " + deepRaces.getNoPairs());
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Shallow races ------------------------------------- ");
		// Bubble up races
		ConcurrentAccesses<ConcurrentAccess<?>> shallowRaces = ConcurrentShallowAccesses.rippleUp(deepRaces);
		Log.log("Rippleup up races in libraries");
		shallowRaces.reduceNonConcurrent();
		System.out.println("---- Shallow races ---- ");
		System.out.println(shallowRaces.toString());
		Log.log("Shallow races done. # = " + shallowRaces.getNoPairs());
		System.out.println("-------------------------------------------------------- \n");

		// Other stuff
		System.out.println("------- Other stuff ------------------------------------ ");
		System.out.println(A.callGraph.getNumberOfNodes());
		System.out.println("-------------------------------------------------------- \n");
		
		return shallowRaces;
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

	public Object traceBackToShared(CGNode predNode, int use) {
		// TODO Auto-generated method stub
		return null;
	}
}
