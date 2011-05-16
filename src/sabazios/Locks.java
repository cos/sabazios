package sabazios;

import java.util.HashMap;
import java.util.Iterator;

import sabazios.lockset.CFG.Solver;
import sabazios.lockset.callGraph.Lock;
import sabazios.util.IntSetVariable;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.intset.IntSetUtil;

public class Locks {
	private final CallGraph callGraph;

	Locks(CallGraph callGraph) {
		this.callGraph = callGraph;
	}

	HashMap<CGNode, Lock> locksForCGNodes = new HashMap<CGNode, Lock>();
	HashMap<IMethod, HashMap<SSAInstruction, IntSetVariable>> intraProceduralLocks = new HashMap<IMethod, HashMap<SSAInstruction, IntSetVariable>>();

	public void compute() {
		inferIntraProcedural();
		inferInterProcedural();
	}

	private void inferInterProcedural() {
		sabazios.lockset.callGraph.Solver solver = new sabazios.lockset.callGraph.Solver(this.callGraph,
				this.intraProceduralLocks);
		try {
			solver.solve(null);
		} catch (CancelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (CGNode n : callGraph) {
			this.locksForCGNodes.put(n, solver.getOut(n));
		}
	}

	private void inferIntraProcedural() {
		for (CGNode n : this.callGraph) {
			HashMap<SSAInstruction, IntSetVariable> insideLocks = intraProceduralLocks.get(n.getMethod());
			if (insideLocks != null)
				continue;

			if (n.getIR() == null)
				continue;
			ExplodedControlFlowGraph explodedCFG = ExplodedControlFlowGraph.make(n.getIR());
			Solver solverEnter = new Solver(explodedCFG, true);
			try {
				solverEnter.solve(null);
			} catch (CancelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Solver solverExit = new Solver(explodedCFG, false);
			try {
				solverExit.solve(null);
			} catch (CancelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			insideLocks = new HashMap<SSAInstruction, IntSetVariable>();

			for (IExplodedBasicBlock iExplodedBasicBlock : explodedCFG) {	
				SSAInstruction i = iExplodedBasicBlock.getInstruction();
				IntSetVariable lockEnter = solverEnter.getOut(iExplodedBasicBlock).clone();
				IntSetVariable lockExit = solverExit.getOut(iExplodedBasicBlock);
				lockEnter.diff(lockExit);
//				if(n.getMethod().toString().contains("BufferedWriter, write(Ljava/lang/String;II)"))  {
//					System.out.println(" FINAL "+iExplodedBasicBlock.getGraphNodeId()+" : "+i+"   :   "+lockEnter);
//				}
				if(i!=null) {
					insideLocks.put(i, lockEnter);
				}
			}
			intraProceduralLocks.put(n.getMethod(), insideLocks);
		}
	}

	public Lock get(CGNode n) {
		return locksForCGNodes.get(n);
	}

	public Lock get(CGNode n, SSAInstruction i) {
		Lock result = (Lock) get(n).clone();
		if (intraProceduralLocks.containsKey(n.getMethod())) {
			HashMap<SSAInstruction, IntSetVariable> hashMap = intraProceduralLocks.get(n.getMethod());
			IntSetVariable intSetVariable = hashMap.get(i);
			result.addNewVars(n, intSetVariable);
		}
		return result;
	}
}
