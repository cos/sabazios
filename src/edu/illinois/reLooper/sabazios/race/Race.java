package edu.illinois.reLooper.sabazios.race;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;

import edu.illinois.reLooper.sabazios.Analysis;
import edu.illinois.reLooper.sabazios.CodeLocation;

public abstract class Race {

	protected final NormalStatement statement;

	public Race(NormalStatement statement) {
		this.statement = statement;
	}

	public NormalStatement getStatement() {
		return statement;
	}

	public String getRaceStackTrace(CallGraph callGraph) {
		NormalStatement statement = this.getStatement();
		return this.getStackTrace(callGraph, statement);
	}

	protected String getStackTrace(CallGraph callGraph, NormalStatement statement) {
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

	@Override
	public String toString() {
		return "RACE " + CodeLocation.make(statement) + " : " + statement;
	}

	public String toDetailedString(CallGraph callGraph) {
		return this.toString();
	}
}