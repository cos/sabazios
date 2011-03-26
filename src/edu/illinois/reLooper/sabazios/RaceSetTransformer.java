package edu.illinois.reLooper.sabazios;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.GraphInverter;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;

import edu.illinois.reLooper.sabazios.race.Race;
import edu.illinois.reLooper.sabazios.race.RaceOnNonStatic;
import edu.illinois.reLooper.sabazios.race.ShallowRace;
import edu.illinois.reLooper.sabazios.util.NodeFinder;

public class RaceSetTransformer {
	private final Analysis analysis;

	public RaceSetTransformer(Analysis analysis) {
		this.analysis = analysis;
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
		if (inApplicationScope(node))
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
				CGNode predNode = (CGNode) predNodes.next();
				if (inApplicationScope(predNode))
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
			CallSiteReference callSiteReference = (CallSiteReference) possibleSites.next();
			IntIterator callInstructionIndices = predNode.getIR().getCallInstructionIndices(callSiteReference)
					.intIterator();
			while (callInstructionIndices.hasNext()) {
				int next = callInstructionIndices.next();
				shallowRaces.add(new ShallowRace(new NormalStatement(predNode, next), r));
			}
		}
	}

	private boolean inApplicationScope(CGNode node) {
		IClassLoader classLoader = node.getMethod().getDeclaringClass().getClassLoader();
		return classLoader.getReference().equals(ClassLoaderReference.Application);
	}
}