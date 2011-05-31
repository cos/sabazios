package sabazios.lockset;

import java.util.LinkedHashMap;
import java.util.Map;

import sabazios.lockset.CFG.Solver;
import sabazios.lockset.callGraph.LockMeet;
import sabazios.lockset.callGraph.LockSetVariable;
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

	public Locks(CallGraph callGraph) {
		this.callGraph = callGraph;
	}

	public Map<CGNode, LockSet> locksForCGNodes = new LinkedHashMap<CGNode, LockSet>();
	Map<IMethod, Map<SSAInstruction, IntSetVariable>> intraProceduralLocks = new LinkedHashMap<IMethod, Map<SSAInstruction, IntSetVariable>>();

	public void compute() {
		try {
			inferIntraProcedural();
			inferInterProcedural();
		} catch (CancelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void inferInterProcedural() throws CancelException {
		sabazios.lockset.callGraph.Solver solver = new sabazios.lockset.callGraph.Solver(this.callGraph,
		    this.intraProceduralLocks);
		solver.solve(null);
		LockMeet.firstPass = false;
		solver.solve(null);
		for (CGNode n : callGraph) {
			LockSet out = solver.getOut(n).getIndividualLocks();
			this.locksForCGNodes.put(n, out);
		}
	}

	private void inferIntraProcedural() throws CancelException {
		for (CGNode n : this.callGraph) {
			Map<SSAInstruction, IntSetVariable> insideLocks = intraProceduralLocks.get(n.getMethod());
			if (insideLocks != null)
				continue;

			if (n.getIR() == null)
				continue;
			ExplodedControlFlowGraph explodedCFG = ExplodedControlFlowGraph.make(n.getIR());
			Solver solverEnter = new Solver(explodedCFG, true);
			solverEnter.solve(null);
			Solver solverExit = new Solver(explodedCFG, false);
			solverExit.solve(null);

			insideLocks = new LinkedHashMap<SSAInstruction, IntSetVariable>();

			for (IExplodedBasicBlock iExplodedBasicBlock : explodedCFG) {
				SSAInstruction i = iExplodedBasicBlock.getInstruction();
				IntSetVariable lockEnter = solverEnter.getOut(iExplodedBasicBlock).clone();
				IntSetVariable lockExit = solverExit.getOut(iExplodedBasicBlock);
				lockEnter.diff(lockExit);
				if (i != null) {
					insideLocks.put(i, lockEnter);
				}
			}
			intraProceduralLocks.put(n.getMethod(), insideLocks);
		}
	}

	public LockSet get(CGNode n) {
		return locksForCGNodes.get(n);
	}

	public LockSet get(CGNode n, SSAInstruction i) {
		LockSet lockSet = new LockSet();
		lockSet.addAll(get(n));
		if (intraProceduralLocks.containsKey(n.getMethod())) {
			Map<SSAInstruction, IntSetVariable> hashMap = intraProceduralLocks.get(n.getMethod());
			IntSetVariable intSetVariable = hashMap.get(i);
			for (Integer v : intSetVariable) {
				lockSet.add(new Lock(n, v));
			}
		}
		return lockSet;
	}
}
