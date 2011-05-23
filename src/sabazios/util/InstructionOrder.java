package sabazios.util;

import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.graph.traverse.BFSIterator;

public class InstructionOrder {
	
	public static boolean happensBefore(CGNode n, SSAInstruction i1, SSAInstruction i2) {
		ISSABasicBlock bb1 = n.getIR().getBasicBlockForInstruction(i1);
		ISSABasicBlock bb2 = n.getIR().getBasicBlockForInstruction(i2);
		SSACFG cfg = n.getIR().getControlFlowGraph();
		BFSIterator<ISSABasicBlock> bfsIterator = new BFSIterator<ISSABasicBlock>(cfg, bb1);
		while (bfsIterator.hasNext()) {
			ISSABasicBlock bb = (ISSABasicBlock) bfsIterator.next();
			if(bb.equals(bb2))
				return true;
		}
		return false;
	}
	
	public static boolean happensBefore(CallGraph cg, CGNode n1, SSAInstruction i1, CGNode n2, SSAInstruction i2) {
		if(n1.equals(n2))
			return happensBefore(n1, i1, i2);
		
		Iterator<CallSiteReference> possibleSites = cg.getPossibleSites(n1, n2);
		while (possibleSites.hasNext()) {
			CallSiteReference callSiteReference = (CallSiteReference) possibleSites.next();
			SSAAbstractInvokeInstruction[] calls = n1.getIR().getCalls(callSiteReference);
			for (SSAAbstractInvokeInstruction i : calls) {
				if(happensBefore(n1, i1, i))
					return true;
			}
		}
		return false;
	}
	
	public static boolean happensStrictlyBefore(CallGraph cg, CGNode n1, SSAInstruction i1, CGNode n2, SSAInstruction i2) {
		return happensBefore(cg, n1, i1, n2, i2) && !happensBefore(cg, n2, i2, n1, i1);
	}
	
//	static LinkedHashMap<IR, InstructionOrder> cached;
//	private final IR ir;
//	
//	private InstructionOrder(IR ir) {
//		this.ir = ir;
//		compute();
//	}
//
//	public static InstructionOrder get(IR ir) {
//		InstructionOrder io = cached.get(ir);
//		if(io == null) { 
//			io = new InstructionOrder(ir);
//			cached.put(ir, io);
//		}
//		return io;
//	}
}
