package edu.illinois.reLooper.sabazios.race;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
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

	public String toStackTraceString(Analysis analysis) {
		NormalStatement statement = this.getStatement();
		return this.getStackTrace(analysis, statement);
	}

	protected String getStackTrace(Analysis analysis,  NormalStatement statement) {
		StringBuilder s = new StringBuilder();
		s.append(CodeLocation.make(statement));
		s.append("\n");
		CGNode node = statement.getNode();
		return s.toString() + getOneStackTraceString(analysis, node);
	}

	private static String getOneStackTraceString(Analysis analysis, CGNode node) {
		CallGraph callGraph = analysis.callGraph;
		HashSet<CGNode> visitedNodes = new HashSet<CGNode>();
		StringBuilder s = new StringBuilder();

		while (callGraph.getPredNodeCount(node) > 0) {
			visitedNodes.add(node);
			CGNode predNode;
			Iterator<CGNode> predNodes = callGraph.getPredNodes(node);
			do {
				predNode = predNodes.next();
			} while (visitedNodes.contains(predNode) && predNodes.hasNext());

			// if (!node.toString().contains("Ljava/")) {
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
						if (!(analysis.traceBackToShared(predNode, use) == null))
							s.append(" : " + CodeLocation.variableName(use, predNode, invoke));
					}
				}
			}
			s.append("\n");
			// }
			node = predNode;
		}
		return s.toString();
	}

	@Override
	public String toString() {
		return "RACE " + CodeLocation.make(statement) + " : " + statement;
	}

	public String toDetailedString(Analysis analysis) {
		return this.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Race))
			return false;
		Race race = (Race) obj;
		return this.statement.getInstruction().equals(race.statement.getInstruction())
				&& this.statement.getNode().getMethod().equals(race.statement.getNode().getMethod());
	}

	@Override
	public int hashCode() {
		return 7057;
	}
}