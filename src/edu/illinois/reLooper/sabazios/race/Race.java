package edu.illinois.reLooper.sabazios.race;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.PartialCallGraph;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.graph.impl.GraphInverter;
import com.ibm.wala.util.graph.traverse.DFS;

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

	public String toStackTraceString(CallGraph callGraph) {
		NormalStatement statement = this.getStatement();
		return this.getStackTrace(callGraph, statement);
	}

	protected String getStackTrace(CallGraph callGraph, NormalStatement statement) {
		StringBuilder s = new StringBuilder();
		s.append(CodeLocation.make(statement));
		s.append("\n");
		CGNode node = statement.getNode();
		HashSet<CGNode> visitedNodes = new HashSet<CGNode>();
		// PartialCallGraph stackTrace = getStackTrace(node, visitedNodes,
		// callGraph);
		return s.toString() + getOneStackTraceString(node, callGraph);
	}

	private static String getOneStackTraceString(CGNode node, CallGraph callGraph) {
		HashSet<CGNode> visitedNodes = new HashSet<CGNode>();
		StringBuilder s = new StringBuilder();

		while (callGraph.getPredNodeCount(node) > 0) {
			visitedNodes.add(node);
			CGNode predNode;
			Iterator<CGNode> predNodes = callGraph.getPredNodes(node);
			do {
			predNode = predNodes.next();
			} while(visitedNodes.contains(predNode) && predNodes.hasNext());
			
//			if (!node.toString().contains("Ljava/")) {
				Iterator<CallSiteReference> possibleSites = callGraph.getPossibleSites(predNode, node);
				while (possibleSites.hasNext()) {
					CallSiteReference callSiteReference = (CallSiteReference) possibleSites.next();
					CodeLocation cl = CodeLocation.make(predNode, callSiteReference.getProgramCounter());
					if (cl != null)
						s.append(cl.toString());
					SSAAbstractInvokeInstruction[] calls = predNode.getIR().getCalls(callSiteReference);
					for (SSAAbstractInvokeInstruction invoke : calls) {
						for (int i = 0; i < invoke.getNumberOfUses(); i++) {
							int use = invoke.getUse(i);
							LocalPointerKey localPointerKey = Analysis.instance.getLocalPointerKey(predNode, use);
							if (!Analysis.getOutsideAllocationSites(predNode, use).isEmpty())
								s.append(" : " + CodeLocation.variableName(use, predNode, invoke));
						}
					}
				}
				s.append("\n");
//			}
			node = predNode;
		}
		return s.toString();
	}

	private static PartialCallGraph getStackTrace(CGNode node, HashSet<CGNode> visitedNodes, CallGraph callGraph) {
		Set<CGNode> reachableNodes = DFS.getReachableNodes(GraphInverter.invert(callGraph), Sets.newHashSet(node));
		return PartialCallGraph.make(callGraph, callGraph.getEntrypointNodes(), reachableNodes);
	}

	@Override
	public String toString() {
		return "RACE " + CodeLocation.make(statement) + " : " + statement;
	}

	public String toDetailedString(CallGraph callGraph) {
		return this.toString();
	}
}