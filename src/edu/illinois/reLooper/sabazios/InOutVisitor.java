package edu.illinois.reLooper.sabazios;

import java.util.EnumMap;
import java.util.HashSet;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAInstruction;

import extra166y.ParallelArray;

public class InOutVisitor extends TraversalVisitor {
	private State state = State.OUT;
	
	private final boolean DEBUG = false;
	
	public HashSet<StatementWithInstructionIndex> out = new HashSet<StatementWithInstructionIndex>();
	public HashSet<StatementWithInstructionIndex> in = new HashSet<StatementWithInstructionIndex>();

	private CGNode parOpCGNode; // the CG node that contains the parallel operation
	
	@Override
	public void startVisiting()
	{
		for (State st : State.values()) {
			nodesSeenBy.put(st, new HashSet<CGNode>());
		}
	}
	
	private enum State {
		IN, OUT
	}

	@Override
	public void visitBefore(CGNode cgNode) {
		if (cgNode.getMethod().toString().contains(ParallelArray.OP_STRING))
		{
			if(getParOpCGNode() == null)
				this.parOpCGNode = cgNode;
			state = State.IN;
			log("Entered IN state");
		}
	}

	@Override
	public void visitAfter(CGNode cgNode) {
		if (cgNode.getMethod().toString().contains(ParallelArray.OP_STRING))
		{
			state = State.OUT;
			log("Exited IN state");
		}
		
	}

	private void log(String message) {
		if(DEBUG)
			System.out.println(message);
	}

	@Override
	public void visit(CGNode cgNode, SSAInstruction i) {
		int instructionIndex = CodeLocation.getSSAInstructionNo(cgNode, i);
		NormalStatement normalStatement = new NormalStatement(cgNode, instructionIndex);
		switch (state) {
			case OUT:
				out.add(normalStatement);
				log("OUT: "+CodeLocation.make(normalStatement));
				break;
			case IN:
				in.add(normalStatement);
				log("IN: "+CodeLocation.make(normalStatement));
				break;
		}
	}

	public CGNode getParOpCGNode() {
		return parOpCGNode;
	}
	
	
	// Visiting logic
	@Override
	public boolean controlVisiting() {
		return true;
	}
	
	EnumMap<State, HashSet<CGNode>> nodesSeenBy = new EnumMap<InOutVisitor.State, HashSet<CGNode>>(State.class);
	@Override
	public void beganVisiting(CGNode node) {
		this.nodesSeenBy.get(state).add(node);
	}
	
	@Override
	public boolean shouldVisit(CGNode node) {
		return !nodesSeenBy.get(state).contains(node);
	}
}