package sabazios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sabazios.domains.AlphaAccesses;
import sabazios.domains.BetaAccesses;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentAccesses;
import sabazios.domains.ConcurrentFieldAccesses;
import sabazios.domains.ConcurrentShallowAccesses;
import sabazios.domains.FilterSafe;
import sabazios.domains.Loops;
import sabazios.domains.PointerForValue;
import sabazios.lockset.Locks;
import sabazios.util.Log;
import sabazios.util.wala.viz.CGNodeDecorator;
import sabazios.util.wala.viz.DotUtil;
import sabazios.util.wala.viz.NodeDecorator;
import sabazios.util.wala.viz.PDFViewUtil;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.warnings.WalaException;

public class A {

	public CallGraph callGraph;
	public PointerAnalysis pointerAnalysis;
	public HeapGraph heapGraph;
	public IClassHierarchy cha;
	public Locks locks;
	public PointerForValue pointerForValue;
	public AlphaAccesses alphaAccesses;
	public BetaAccesses betaAccesses;
	public final Loops loops;

	public A(CallGraph callGraph, PointerAnalysis pointerAnalysis) {
		this.callGraph = callGraph;
		this.pointerAnalysis = pointerAnalysis;
		this.heapGraph = this.pointerAnalysis.getHeapGraph();
		this.cha = this.pointerAnalysis.getClassHierarchy();

		this.pointerForValue = new PointerForValue();
		this.alphaAccesses = new AlphaAccesses(this);
		this.betaAccesses = new BetaAccesses(this);
		this.loops = new Loops();
		this.locks = new Locks(this);
	}

	public ConcurrentAccesses<?> compute() {
		// Graph<Integer> prunedM = GraphSlicer.prune(m, new
		// Filter<Integer>() {
		// @Override
		// public boolean accepts(Integer o) {
		// return m.getSuccNodeCount(o) > 0 || m.getPredNodeCount(o) > 0;
		// }
		// });

		precompute();

		System.out.println("---- Reads and writes ---------------------------------- ");
		alphaAccesses.compute(this);
		Log.log("Found all relevant writes: " + alphaAccesses.size());
		betaAccesses.compute(this);
		Log.log("Found all relevant other accesses: " + betaAccesses.size());
		Log.reportTime(":alpha_beta_accesses_time");
		Log.report(":alpha_accesses",alphaAccesses.size());
		Log.report(":beta_accesses",betaAccesses.size());
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Compute locks ------------------------------------- ");
		locks.compute();
//		for (CGNode n : locks.locksForCGNodes.keySet()) {
//			Set<Lock> lock = locks.locksForCGNodes.get(n);
//			if (!lock.isEmpty())
//				System.out.println(n + " -- " + lock);
//		}
		// dotCallGraph(); decoreator to use: new CGNodeDecorator(this)
		// dotIRFor(".*op().*");
		
		Log.reportTime(":locks_time");
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Potential races ------------------------------------- ");
		ConcurrentFieldAccesses potentialRaces = new ConcurrentFieldAccesses(this);
		potentialRaces.compute(this.alphaAccesses, this.betaAccesses);  
		// Distribute locks
		potentialRaces.distributeLocks(this);
		System.out.println(potentialRaces.toString());
		System.out.println();
		int noPotentialRaces = potentialRaces.getNoPairs();
		Log.log("Potential races. # = " + noPotentialRaces);
		Log.reportTime(":potential_races_time");
		Log.report(":potential_races",noPotentialRaces);		
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Deep races ---------------------------------------- ");
		// Lock identity
		// LockIdentity li = new LockIdentity(this);
		// li.compute();
		FilterSafe.filter(this, potentialRaces);
		ConcurrentFieldAccesses deepRaces = potentialRaces;
		System.out.println(deepRaces.toString());
		System.out.println();
		int noRaces = deepRaces.getNoPairs();
		Log.log("Deep races done. # = " + noRaces);
		Log.reportTime(":races_time");
		Log.report(":races",noRaces);
		System.out.println("-------------------------------------------------------- \n");

		System.out.println("---- Shallow races ------------------------------------- ");
		// Bubble up races
		ConcurrentAccesses<ConcurrentAccess<?>> shallowRaces = ConcurrentShallowAccesses.rippleUp(deepRaces);
		Log.log("Rippleup up races in libraries");
		shallowRaces.reduceNonConcurrent();
		System.out.println("---- Shallow races ---- ");
		System.out.println(shallowRaces.toString());
		int noAtomicityViolations = shallowRaces.getNoPairs();
		Log.log("Shallow races done. # = " + noAtomicityViolations);
		int noPrintedAtomicityViolations = shallowRaces.getNoUniquePrintedPairs();
		Log.log("Shallow races printed. # = " + noPrintedAtomicityViolations);
		Log.reportTime(":atomicity_violations_time");
		Log.report(":atomicity_violations",""+noAtomicityViolations);
		Log.report(":printed_atomicity_violations",""+noPrintedAtomicityViolations);
		System.out.println("-------------------------------------------------------- \n");

		// Other stuff
		System.out.println("------- Other stuff ------------------------------------ ");
		int numberOfCGNodes = this.callGraph.getNumberOfNodes();
		System.out.println(numberOfCGNodes);
		Log.report(":call_graph_nodes", numberOfCGNodes);
		System.out.println("-------------------------------------------------------- \n");
		
		
		interactiveDebug();
		
		return shallowRaces;
	}

	public void precompute() {
		System.out.println("---- Compute map value -> PointerKey ------------------- ");
		pointerForValue.compute(this.heapGraph);
		Log.log("Function CGNode x SSAvalue -> Object precomputed");
		Log.reportTime(":map_vars_to_pointers_time");
		System.out.println("-------------------------------------------------------- \n");
	}

	private void interactiveDebug() {
	  String s = "";
		do {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			System.out.println("What do you want to detail (regex)? \n");
			try {
	      s = br.readLine();
	      s = ".*"+s+".*";
	      List<CGNode> nodes = findNodes(s);
	      
	      if(nodes.size() == 0) {
	      	System.out.println("No nodes found. Try again.");
	      	continue;
	      } 
	      
	      System.out.println("Found "+nodes.size()+" nodes:");
	      for(int i=0;i<nodes.size();i++)
	      	System.out.println(i+" : "+nodes.get(i).toString());	        
       
	      int x = -1;
	      do {
	      	System.out.println("Which one interests you? ");
		      String s1 = br.readLine();
		      try {
		      x = Integer.parseInt(s1);
		      } catch(NumberFormatException e) {
		      	System.out.println("Number not recognized. Try again.");
		      	continue;
		      }
		      if(x < 0 || x >= nodes.size())
		      	System.out.println("Node index not in range. Try aganin.");
	      } while(x == -1);
	      
	      final CGNode theNode = nodes.get(x); 
	      
	      final Set<CGNode> predecedorNodes = GraphSlicer.slice(callGraph, new Filter<CGNode>() {
					@Override
          public boolean accepts(CGNode o) {
	          return o == theNode;
          }
	      });
	      Graph<CGNode> prunedCallGraph = GraphSlicer.prune(callGraph, new Filter<CGNode>() {
					@Override
          public boolean accepts(CGNode o) {
	          return predecedorNodes.contains(o);
          }
	      });
	      dotGraph(prunedCallGraph, s, new CGNodeDecorator(this));
	      
      } catch (IOException e) {
	      e.printStackTrace();
      }
		} while(s.length() > 4);
  }

	@SuppressWarnings("unused")
	public void dotIRFor(String s) {
		for (CGNode cgNode : callGraph) {
			if (cgNode.getMethod().toString().matches(s))
				try {
					System.out.println("Generated: " + "debug/irPdf" + cgNode.getGraphNodeId());

					PDFViewUtil.ghostviewIR(cha, cgNode, "debug/irPdf" + cgNode.getGraphNodeId(), "./debug/irDot"
							+ cgNode.getGraphNodeId(), "dot", "open");
				} catch (WalaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public void dotGraph(Graph<?> m, String name, NodeDecorator decorator) {
		String dotFile = "debug/" + name + ".dot";
		String outputFile = "./debug/" + name + ".pdf";
		try {
			DotUtil.dotify(m, decorator, dotFile, outputFile, "dot");
			PDFViewUtil.launchPDFView(outputFile, "open");
		} catch (WalaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public List<CGNode> findNodes(String regex) {
		Iterator<CGNode> iterator = callGraph.iterator();
		ArrayList<CGNode> nodes = new ArrayList<CGNode>();
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
