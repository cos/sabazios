package edu.illinois.reLooper.sabazios;

import java.util.HashSet;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAInstruction;

public class BeforeInAfterVisitor extends TraversalVisitor {
	private State state = State.BEFORE;
	
	public HashSet<StatementWithInstructionIndex> before = new HashSet<StatementWithInstructionIndex>();
	public HashSet<StatementWithInstructionIndex> in = new HashSet<StatementWithInstructionIndex>();
	public HashSet<StatementWithInstructionIndex> after = new HashSet<StatementWithInstructionIndex>();

	private CGNode parOpCGNode; // the CG node that contains the parallel operation
	
	private enum State {
		BEFORE, IN, AFTER
	}

	@Override
	public void visitBefore(CGNode cgNode) {
		if (cgNode.getMethod().toString().contains("op("))
		{
			if(getParOpCGNode() == null)
				this.parOpCGNode = cgNode;
			state = State.IN;
		}
	}

	@Override
	public void visitAfter(CGNode cgNode) {
		if (cgNode.getMethod().toString().contains("op("))
			state = State.AFTER;
	}

	@Override
	public void visit(CGNode cgNode, SSAInstruction i) {
		int instructionIndex = CodeLocation.getSSAInstructionNo(cgNode, i);
		switch (state) {
			case BEFORE:
				before.add(new NormalStatement(cgNode, instructionIndex));
				break;
			case IN:
				in.add(new NormalStatement(cgNode, instructionIndex));
				break;
			case AFTER:
				after.add(new NormalStatement(cgNode, instructionIndex));
				break;
		}
	}

	public CGNode getParOpCGNode() {
		return parOpCGNode;
	}
}