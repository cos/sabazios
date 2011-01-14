package edu.illinois.reLooper.sabazios;

import java.util.HashSet;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

public abstract class TraversalVisitor {
	public abstract void visitBefore(CGNode cgNode);

	public abstract void visitAfter(CGNode cgNode);

	public abstract void visit(CGNode cgNode, SSAInstruction i);

	/**
	 * Should the traversal visitor control the revisiting of nodes? A CGNode is
	 * visited only once by default (if controlVisiting() returns false). If it
	 * returns true, the visitor has control over which nodes should be visited and
	 * a node can be visited multiple times.
	 * 
	 * @return
	 */
	public boolean controlVisiting() {
		return false;
	}
	
	HashSet<CGNode> visitedNodes;
	/**
	 * The default behaviour is for a node to be visited once. This can be overridden in
	 * implementing classes.
	 *  
	 * @return
	 */
	public boolean shouldVisit(CGNode node) {
		return !visitedNodes.contains(node);
	}
	
	public void beganVisiting(CGNode node) {
		visitedNodes.add(node);
	}

	/**
	 * Invoked by the program traverser just before starting the traversal.
	 */
	public void startVisiting() {
		visitedNodes = new HashSet<CGNode>();
		
	}

}
