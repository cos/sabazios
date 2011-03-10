package edu.illinois.reLooper.sabazios;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;

public class Race {
	private final NormalStatement statement;
	private final InstanceKey instanceKey;
	private final boolean isLoopCarriedDependency;

	/**
	 * Describes a race.
	 * 
	 * If instanceKey is null, this a race to a static field.
	 * 
	 * @param statement
	 * @param instanceKey
	 */
	public Race(NormalStatement statement, InstanceKey instanceKey, boolean isLoopCarriedDependency) {
		this.statement = statement;
		this.instanceKey = instanceKey;
		this.isLoopCarriedDependency = isLoopCarriedDependency;
	}

	@Override
	public String toString() {
		AllocationSiteInNode allocationSite = (AllocationSiteInNode) instanceKey;
		CodeLocation allocationLocation;
		if (instanceKey != null)
			allocationLocation = CodeLocation.make(allocationSite.getNode(), allocationSite.getSite()
					.getProgramCounter());
		else
			allocationLocation = null;

		return (isStatic() ? "STATIC " : "") + "RACE " + (isLoopCarriedDependency ? " with LCD " : "")
				+ CodeLocation.make(getStatement()) + " : " + getStatement() + " write to " + allocationLocation;
	}

	public String toDetailedString(CallGraph callGraph) {
		StringBuffer s = new StringBuffer();
		s.append(this);
		s.append("\n");
		s.append(this.getRaceStackTrace(callGraph));
		s.append("\n");
		s.append("Allocation site\n");
		s.append(this.getAllocationStackTrace(callGraph));
		s.append("\n");
		return s.toString();
	}

	public boolean isStatic() {
		return getInstanceKey() == null;
	}

	public boolean isLoopCarriedDependency() {
		return isLoopCarriedDependency;
	}

	public NormalStatement getStatement() {
		return statement;
	}

	public InstanceKey getInstanceKey() {
		return instanceKey;
	}

	public String getRaceStackTrace(CallGraph callGraph) {
		NormalStatement statement = this.getStatement();
		return this.getStackTrace(callGraph, statement);
	}

	public String getAllocationStackTrace(CallGraph callGraph) {
		AllocationSiteInNode allocationSite = (AllocationSiteInNode) instanceKey;
		if (instanceKey == null)
			return "STATIC";
		return getStackTrace(callGraph, Analysis.getNormalStatement(allocationSite));
	}

	private String getStackTrace(CallGraph callGraph, NormalStatement statement) {
		StringBuilder s = new StringBuilder();
		s.append(CodeLocation.make(statement));
		s.append("\n");
		CGNode node = statement.getNode();
		HashSet<CGNode> visitedNodes = new HashSet<CGNode>();
		getStackTrace(node, visitedNodes, callGraph, s);
		return s.toString();
	}

	private void getStackTrace(CGNode node, HashSet<CGNode> visitedNodes, CallGraph callGraph, StringBuilder s) {
		if (visitedNodes.contains(node))
			return;
		visitedNodes.add(node);
		Iterator<CGNode> predNodes = callGraph.getPredNodes(node);
		while (predNodes.hasNext()) {
			CGNode cgNode = (CGNode) predNodes.next();
			if (!cgNode.toString().contains("Ljava/")) {
				Iterator<CallSiteReference> possibleSites = callGraph.getPossibleSites(cgNode, node);
				while (possibleSites.hasNext()) {
					CallSiteReference callSiteReference = (CallSiteReference) possibleSites.next();
					CodeLocation cl = CodeLocation.make(cgNode, callSiteReference.getProgramCounter());
					if (cl != null)
						s.append(cl.toString());
					SSAAbstractInvokeInstruction[] calls = cgNode.getIR().getCalls(callSiteReference);
					for (SSAAbstractInvokeInstruction invoke : calls) {
						for(int i = 0; i<invoke.getNumberOfUses(); i++)
						{
							int use = invoke.getUse(i);
							LocalPointerKey localPointerKey = Analysis.instance.getLocalPointerKey(cgNode, use);
							if(!Analysis.getOutsideAllocationSites(cgNode, use).isEmpty())
								s.append(" : "+CodeLocation.variableName(use, cgNode, invoke));
						}
					}
					s.append("\n");
				}
			}
			getStackTrace(cgNode, visitedNodes, callGraph, s);
		}
	}
}
