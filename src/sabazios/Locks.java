package sabazios;

import java.util.HashMap;

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
			this.locksForCGNodes.put(n, solver.getIn(n));
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
			Solver solver = new Solver(explodedCFG);
			try {
				solver.solve(null);
			} catch (CancelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			insideLocks = new HashMap<SSAInstruction, IntSetVariable>();
			intraProceduralLocks.put(n.getMethod(), insideLocks);

			for (IExplodedBasicBlock iExplodedBasicBlock : explodedCFG) {
				SSAInstruction i = iExplodedBasicBlock.getInstruction();
				IntSetVariable lock = solver.getOut(iExplodedBasicBlock);
				insideLocks.put(i, lock);
			}
		}
	}

	public Lock get(CGNode n) {
		return locksForCGNodes.get(n);
	}

	public Lock get(CGNode n, SSAInstruction i) {
		Lock result = (Lock) get(n).clone();
		if (intraProceduralLocks.containsKey(n)) {
			IntSetVariable intSetVariable = intraProceduralLocks.get(n).get(i);
			result.addNewVars(n, intSetVariable);
		}
		return result;
	}
}
