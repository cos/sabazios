package edu.illinois.reLooper.sabazios;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAInstruction;

public class SuccUseVisitor extends TraversalVisitor {
	/**
	 * 
	 */
	private final BeforeInAfterVisitor beforeInAfter;
	private final AllocationSiteInNode instanceKey;
	private final StatementWithInstructionIndex statement;
	boolean after = false;
	boolean foundUse = false;
	private final HashSet<StatementWithInstructionIndex> uses;
	private final Analysis analysis;

	public SuccUseVisitor(Analysis analysis, BeforeInAfterVisitor beforeInAfter,
			AllocationSiteInNode instanceKey,
			StatementWithInstructionIndex statement) {
		this.analysis = analysis;
		this.beforeInAfter = beforeInAfter;
		this.instanceKey = instanceKey;
		this.statement = statement;
		this.uses = new HashSet<StatementWithInstructionIndex>();
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
	public void visit(CGNode cgNode, SSAInstruction instr) {
		NormalStatement normalStatement = new NormalStatement(cgNode, CodeLocation.getSSAInstructionNo(cgNode, instr));
		if(normalStatement.equals(statement))
			after = true;
		
		if(after && beforeInAfter.in.contains(normalStatement))
		{
			if(analysis.doesStatementUseInstanceKey(normalStatement, instanceKey))
				uses.add(normalStatement);
		}
	}

	public HashSet<StatementWithInstructionIndex> getUses() {
		return uses;
	}
}