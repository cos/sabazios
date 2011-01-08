package edu.illinois.reLooper.sabazios;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

public abstract class TraversalVisitor {
	public abstract void visitBefore(CGNode cgNode);
	public abstract void visitAfter(CGNode cgNode);
	public abstract void visit(CGNode cgNode, SSAInstruction i);
}
