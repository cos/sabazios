package sabazios;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.Loop;
import sabazios.domains.ObjectAccess;
import sabazios.domains.WriteFieldAccess;
import sabazios.lockIdentity.FieldEdge;
import sabazios.lockIdentity.InferDereferences;
import sabazios.lockIdentity.MustAliasHeapMethodSummary;
import sabazios.lockIdentity.ValueNode;
import sabazios.lockset.callGraph.Lock;
import sabazios.util.CodeLocation;
import sabazios.util.IntSetVariable;
import sabazios.util.Log;
import sabazios.util.Tuple;
import sabazios.util.Tuple.Pair;
import sabazios.util.U;
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
import com.ibm.wala.model.java.lang.reflect.Array;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.GraphUtil;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.warnings.WalaException;

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
		PDFViewUtil.raceAnalysis = this;

		CGNode node = findNodes(".*simple.*").iterator().next();
		InferDereferences id = new InferDereferences(node, 8);
		LinkedHashSet<ArrayDeque<Pair<ValueNode, FieldEdge>>> infer = id.infer();
		for (ArrayDeque<Pair<ValueNode, FieldEdge>> arrayDeque : infer) {
			System.out.println(arrayDeque.peek().p1());
			for (Pair<ValueNode, FieldEdge> pair : arrayDeque) {
				FieldEdge p2 = pair.p2();
				if (p2 != null)
					System.out.print("." + p2.f.getName());
			}
			System.out.println();
		}
//		Graph<ValueNode> prunedM = GraphSlicer.prune(m, new Filter<ValueNode>() {
//			@Override
//			public boolean accepts(ValueNode o) {
//				return m.getSuccNodeCount(o) > 0 || m.getPredNodeCount(o) > 0;
//			}
//		});

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
		System.out.println(this.callGraph.getNumberOfNodes());
		System.out.println("-------------------------------------------------------- \n");
	}

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
