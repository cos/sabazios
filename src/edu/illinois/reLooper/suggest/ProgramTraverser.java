package edu.illinois.reLooper.suggest;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class ProgramTraverser {

	private CallGraph callGraph;
	private final CGNode entryNode;
	private final TraversalVisitor visitor;

	public ProgramTraverser(CallGraph callGraph, CGNode entryNode, TraversalVisitor cgNodeVisitor) {
		this.callGraph = callGraph;
		this.entryNode = entryNode;
		this.visitor = cgNodeVisitor;
	}

	HashSet<CGNode> visitedNodes = new HashSet<CGNode>();

	public void traverse() {
		CGNodeTraverser cgNodeTraverser = new CGNodeTraverser(entryNode);
		cgNodeTraverser.traverse();
	}

	class CGNodeTraverser {
		private HashSet<ISSABasicBlock> visitedBlocks;
		private final CGNode cgNode;

		public CGNodeTraverser(CGNode cgNode) {
			this.cgNode = cgNode;
			visitedBlocks = new HashSet<ISSABasicBlock>();
		}

		public void traverse() {
//			System.out.println(cgNode);
//			System.out.println(cgNode.getMethod());
			visitor.visitBefore(cgNode);
			
			if (cgNode.getIR() != null)
				visit(cgNode.getIR().getControlFlowGraph().entry());
			
			visitor.visitAfter(cgNode);
		}

		private void visit(ISSABasicBlock bb) {
			if (visitedBlocks.contains(bb))
				return;
			visitedBlocks.add(bb);
			for (SSAInstruction instr : bb) {
				visitor.visit(cgNode, instr);
//				if (!cgNode.getMethod().getDeclaringClass().getClassLoader().toString().equals("Primordial"))
//					System.out.println(" --> "
//							+ CodeLocation.make(cgNode, instr)
//							+ " : " + instr);
				

				if (instr instanceof SSAInvokeInstruction) {
					SSAInvokeInstruction invokeInstr = (SSAInvokeInstruction) instr;
					CallSiteReference callSite = invokeInstr.getCallSite();
					Set<CGNode> possibleTargets = callGraph.getPossibleTargets(
							cgNode, callSite);
					for (CGNode childNode : possibleTargets)
						if (!visitedNodes.contains(childNode)) {
							visitedNodes.add(childNode);
							CGNodeTraverser cgNodeTraverser = new CGNodeTraverser(
									childNode);
							cgNodeTraverser.traverse();
						}
				}
			}
			Iterator<ISSABasicBlock> succNodes = cgNode.getIR()
					.getControlFlowGraph().getSuccNodes(bb);
			while (succNodes.hasNext()) {
				visit((ISSABasicBlock) succNodes.next());
			}
		}
	}
}
