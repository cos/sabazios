package edu.illinois.reLooper.sabazios;

import java.util.Iterator;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAInstruction;

final class PredUseVisitor extends TraversalVisitor {
	/**
	 * 
	 */
	private final Analysis analysis;
	private final AllocationSiteInNode instanceKey;
	private final StatementWithInstructionIndex statement;
	boolean before = true;
	boolean foundUse = false;

	PredUseVisitor(Analysis analysis,
			AllocationSiteInNode instanceKey,
			StatementWithInstructionIndex statement) {
		this.analysis = analysis;
		this.instanceKey = instanceKey;
		this.statement = statement;
	}

	@Override
	public void visitBefore(CGNode cgNode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitAfter(CGNode cgNode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CGNode cgNode1, SSAInstruction instr) {
		NormalStatement normalStatement = new NormalStatement(cgNode1,
				CodeLocation.getSSAInstructionNo(cgNode1, instr));
		if (normalStatement.equals(statement))
			before = false;

//		if (before && beforeInAfter.in.contains(normalStatement)) {
//			InstanceKey instanceKey = this.instanceKey;
//			foundUse = foundUse
//					|| analysis.doesStatementUseInstanceKey(normalStatement, instanceKey);
//		}
	}
}