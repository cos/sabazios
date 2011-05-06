package edu.illinois.reLooper.sabazios;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sabazios.util.U;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.util.intset.IntIterator;

import edu.illinois.reLooper.sabazios.raceObjects.Race;
import edu.illinois.reLooper.sabazios.raceObjects.ShallowRace;

public class RaceSetTransformer {
	private final Analysis analysis;

	public RaceSetTransformer(Analysis analysis2) {
		this.analysis = analysis2;
	}

	public Set<Race> transform(Set<Race> deepRaces) {
		HashSet<Race> shallowRaces = new HashSet<Race>();
		for (Race r : deepRaces) {
			shallowRaces.addAll(getShallowRaces(r));
		}
		return shallowRaces;
	}

	private Set<Race> getShallowRaces(Race r) {
		CGNode node = r.getStatement().getNode();
		if (U.inApplicationScope(node))
			return Sets.newHashSet(r);

		HashSet<Race> shallowRaces = new HashSet<Race>();
		HashSet<CGNode> visitedNodes = new HashSet<CGNode>();
		ArrayDeque<CGNode> workList = new ArrayDeque<CGNode>();
		workList.add(node);
		while (!workList.isEmpty()) {
			node = workList.pop();
			visitedNodes.add(node);
			Iterator<CGNode> predNodes = analysis.callGraph.getPredNodes(node);
			while (predNodes.hasNext()) {
				CGNode predNode = predNodes.next();
				if (U.inApplicationScope(predNode))
					addRaces(predNode, node, shallowRaces, r);
				else
					if(!visitedNodes.contains(predNode))
					workList.add(predNode);
			}
		}

		return shallowRaces;
	}

	/**
	 * Adds races in predNode that occurred somewhere below node
	 * 
	 * @param node
	 *            - a node in another scope
	 * @param shallowRaces
	 * @param predNode
	 *            - a node in Application scope
	 * @param r 
	 */
	private void addRaces(CGNode predNode, CGNode node, HashSet<Race> shallowRaces, Race r) {
		Iterator<CallSiteReference> possibleSites = analysis.callGraph.getPossibleSites(predNode, node);
		while (possibleSites.hasNext()) {
			CallSiteReference callSiteReference = possibleSites.next();
			IntIterator callInstructionIndices = predNode.getIR().getCallInstructionIndices(callSiteReference)
					.intIterator();
			while (callInstructionIndices.hasNext()) {
				int next = callInstructionIndices.next();
				ShallowRace sr = new ShallowRace(new NormalStatement(predNode, next), r);
				shallowRaces.add(sr);
			}
		}
	}
}