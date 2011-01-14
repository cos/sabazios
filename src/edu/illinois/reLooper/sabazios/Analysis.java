package edu.illinois.reLooper.sabazios;

import java.util.Iterator;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

public class Analysis {
	
	final CallGraph callGraph;
	final PointerAnalysis pointerAnalysis;
	private HeapGraph heapGraph;
	private final BeforeInAfterVisitor beforeInAfter;
	final PropagationCallGraphBuilder builder;

	public Analysis(CallGraph callGraph, PointerAnalysis pointerAnalysis, PropagationCallGraphBuilder builder, BeforeInAfterVisitor beforeInAfter) {
		this.callGraph = callGraph;
		this.pointerAnalysis = pointerAnalysis;
		this.builder = builder;
		this.beforeInAfter = beforeInAfter;
		this.heapGraph = this.pointerAnalysis.getHeapGraph();
	}
	
	// TODO: ssa write to array index instruction

	public LocalPointerKey getLocalPointerKey(CGNode cgNode, int value) {
		LocalPointerKey localPK = null;
		for (Object o : heapGraph) {
			if (!(o instanceof LocalPointerKey))
				continue;

			localPK = (LocalPointerKey) o;
			if (!localPK.getNode().equals(cgNode))
				continue;
			if (localPK.getValueNumber() == value)
				return localPK;
		}
//		System.out.println("Couldn't find heap Instance Key for " + cgNode
//				+ "   value " + value);
		return null;
	}
	
	public boolean doesStatementUseInstanceKey(
			StatementWithInstructionIndex normalStatement, InstanceKey instanceKey) {
		int instructionIndex = normalStatement.getInstructionIndex();
		CGNode cgNode = normalStatement.getNode();
		SSAInstruction predInstruction = cgNode.getIR().getControlFlowGraph()
				.getInstructions()[instructionIndex];
		if(predInstruction instanceof SSAPutInstruction)
			return false;
		for (int i = 0; i < predInstruction.getNumberOfUses(); i++) {
			LocalPointerKey localPointerKey = getLocalPointerKey(cgNode,
					predInstruction.getUse(i));
			
			Iterator<Object> instanceKeys = heapGraph.getSuccNodes(localPointerKey);
			if (instanceKeys == null)
				continue;
			while (instanceKeys.hasNext()) {
				Object object = (Object) instanceKeys.next();
				if (object.equals(instanceKey))
					return true;
			}
		}
		return false;
	}

	public BeforeInAfterVisitor getBeforeInAfter() {
		return beforeInAfter;
	}
	
	public static NormalStatement getNormalStatement(AllocationSiteInNode instanceKey) {
		SSANewInstruction allocationSiteInstruction = getInstruction(instanceKey);
		int allocationSiteInstructionIndex = CodeLocation.getSSAInstructionNo(
				instanceKey.getNode(), allocationSiteInstruction);
		NormalStatement allocationSiteStatement = new NormalStatement(
				instanceKey.getNode(), allocationSiteInstructionIndex);
		return allocationSiteStatement;
	}

	public static SSANewInstruction getInstruction(AllocationSiteInNode p) {
		if (p.getNode().getIR() != null)
			return p.getNode().getIR().getNew(p.getSite());
		else
			return null;
	}
}
