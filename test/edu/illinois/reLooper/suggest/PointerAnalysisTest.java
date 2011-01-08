package edu.illinois.reLooper.suggest;
/*******************************************************************************

 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * This file is a derivative of code released by the University of
 * California under the terms listed below.  
 *
 * Refinement Analysis Tools is Copyright (c) 2007 The Regents of the
 * University of California (Regents). Provided that this notice and
 * the following two paragraphs are included in any distribution of
 * Refinement Analysis Tools or its derivative work, Regents agrees
 * not to assert any of Regents' copyright rights in Refinement
 * Analysis Tools against recipient for recipient's reproduction,
 * preparation of derivative works, public display, public
 * performance, distribution or sublicensing of Refinement Analysis
 * Tools and derivative works, in source code and object code form.
 * This agreement not to assert does not confer, by implication,
 * estoppel, or otherwise any license or rights in any intellectual
 * property of Regents, including, but not limited to, any patents
 * of Regents or Regents' employees.
 * 
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT,
 * INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES,
 * INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE
 * AND ITS DOCUMENTATION, EVEN IF REGENTS HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *   
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE AND FURTHER DISCLAIMS ANY STATUTORY
 * WARRANTY OF NON-INFRINGEMENT. THE SOFTWARE AND ACCOMPANYING
 * DOCUMENTATION, IF ANY, PROVIDED HEREUNDER IS PROVIDED "AS
 * IS". REGENTS HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.cfg.cdg.ControlDependenceGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeBT.GotoInstruction;
import com.ibm.wala.shrikeBT.InvokeInstruction;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;

import edu.illinois.reLooper.suggest.Analysis;
import edu.illinois.reLooper.suggest.BeforeInAfterVisitor;
import edu.illinois.reLooper.suggest.ProgramTraverser;
import edu.illinois.reLooper.suggest.Race;
import edu.illinois.reLooper.suggest.RaceFinder;
import edu.illinois.reLooper.suggest.SuccUseVisitor;

public class PointerAnalysisTest extends DataRaceAnalysisTest {

	private HeapGraph hg;
	private Graph<Object> phg;
	private ControlDependenceGraph<SSAInstruction, ISSABasicBlock> cdg;
	private HashSet<CGNode> visitedCGNodes;
	private HashMap<CGNode, Integer> nodes;
	private CGNode forNode;
	private CGNode parOpCGNode;
	private HashSet<CGNode> taintedNodes;
	private CGNode theNode;
	private HashSet<StatementWithInstructionIndex> uses;

	public PointerAnalysisTest() {
		super("binaryDir,play", "play");
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testClassifyInstructions() throws Exception {
		setup("Play", "main()V");

		BeforeInAfterVisitor beforeInAfter = new BeforeInAfterVisitor();
		ProgramTraverser programTraverser = new ProgramTraverser(callGraph,
				entryMethod, beforeInAfter);
		programTraverser.traverse();

		this.parOpCGNode = beforeInAfter.getParOpCGNode();

		heapGraph = pointerAnalysis.getHeapGraph();

		Analysis analysis = new Analysis(callGraph, pointerAnalysis, beforeInAfter);
		RaceFinder raceFinder = new RaceFinder(analysis);
		Set<Race> races = raceFinder.findRaces(beforeInAfter);

		for (Race race : races) {
			findSynchronizationBlock(analysis, race);
		}
	}

	private void findSynchronizationBlock(
			Analysis analysis, Race race) {
		uses = new HashSet<StatementWithInstructionIndex>();
		taintedNodes = new HashSet<CGNode>();

		for (StatementWithInstructionIndex statement : analysis.getBeforeInAfter().in) {
			if (!statement.toString().contains("Primitive")
					&& analysis.doesStatementUseInstanceKey(statement,
							race.getInstanceKey())
					&& !(statement.getInstruction() instanceof SSAInvokeInstruction)) {
				uses.add(statement);
				taintedNodes.add(statement.getNode());
				// System.out.println(" --> " + CodeLocation.make(statement)
				// + " : " + statement);
			}
		}

		uses.add(race.getStatement());

		nodes = new HashMap<CGNode, Integer>();
		for (CGNode node : taintedNodes) {
			visitedCGNodes = new HashSet<CGNode>();
			visitUp(node);
		}
		// for (CGNode node : nodes.keySet())
		// System.out.println(nodes.get(node) + "   ---  " + node);

		visitedCGNodes = new HashSet<CGNode>();
		visitDown(parOpCGNode);

		System.out.println();
		System.out.println(race);
		System.out.println(" THE NODE: " + this.theNode);
		for (StatementWithInstructionIndex statementWithInstructionIndex : uses) {
			if (statementWithInstructionIndex.getNode()
					.equals(this.theNode))
				System.out.println(" --> "
						+ CodeLocation.make(statementWithInstructionIndex)
						+ " : " + statementWithInstructionIndex);
		}
		System.out.println();
	}

	private void visitDown(CGNode node) {
		if (visitedCGNodes.contains(node))
			return;
		visitedCGNodes.add(node);
		this.theNode = node;

		Iterator<CGNode> succNodes = callGraph.getSuccNodes(node);
		while (succNodes.hasNext()) {
			CGNode cgNode = (CGNode) succNodes.next();
			if (nodes.containsKey(cgNode)
					&& nodes.get(cgNode) == taintedNodes.size()) {
				visitDown(cgNode);
			}
		}
	}

	private void visitUp(CGNode node) {
		if (visitedCGNodes.contains(node))
			return;
		visitedCGNodes.add(node);
		nodes.put(node, nodes.containsKey(node) ? nodes.get(node) + 1 : 1);
		Iterator<CGNode> predNodes = callGraph.getPredNodes(node);
		if (node.equals(parOpCGNode))
			return;
		while (predNodes.hasNext()) {
			CGNode cgNode = (CGNode) predNodes.next();

			Iterator<CallSiteReference> iterateCallSites = cgNode
					.iterateCallSites();
			while (iterateCallSites.hasNext()) {
				CallSiteReference callSiteReference = (CallSiteReference) iterateCallSites
						.next();
				SSAAbstractInvokeInstruction[] calls = cgNode.getIR().getCalls(
						callSiteReference);
				for (SSAAbstractInvokeInstruction ssaAbstractInvokeInstruction : calls) {
					Set<CGNode> possibleTargets = callGraph.getPossibleTargets(
							cgNode, ssaAbstractInvokeInstruction.getCallSite());
					if (possibleTargets.contains(node)) {
						NormalStatement st = new NormalStatement(cgNode,
								CodeLocation.getSSAInstructionNo(cgNode,
										calls[0]));
						uses.add(st);
					}
				}

			}

			visitUp(cgNode);
		}
	}

	@Test
	public void testPrintHeapGraph() throws Exception {
		setup("Play", "main()V");
		hg = pointerAnalysis.getHeapGraph();

		phg = GraphSlicer.prune(hg, new Filter<Object>() {

			@Override
			public boolean accepts(Object o) {
				// System.out.println(": "+o.getClass()+" ||| "+o);
				return !o.toString().contains("Primordial");
			}
		});

		System.out.println(GraphPrint.genericToString(phg));

		// Play with a loop, detect races
		// detectRaces();
	}
}
