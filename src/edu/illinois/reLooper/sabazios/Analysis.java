package edu.illinois.reLooper.sabazios;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.AbstractPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.shrikeBT.Instruction;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.GraphInverter;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;

public class Analysis {

	private static final class PathToSharedFinder extends BFSPathFinder<Object> {
		private final Set<InstanceKey> currentElement;
		private final FlexibleContext currentContext;
		
		@SuppressWarnings("deprecation")
		private PathToSharedFinder(Graph<Object> G, Iterator<Object> nodes, Set<InstanceKey> currentElement, final FlexibleContext currentContext) {
			super(G, nodes, new Filter<Object>() {
				@Override
				public boolean accepts(Object iK) {
					return (iK instanceof AllocationSiteInNode) && isAllocationSharedInContext(iK, currentContext);
				}
			});
			this.currentElement = currentElement;
			this.currentContext = currentContext;
		}

		@Override
		protected Iterator<? extends Object> getConnected(Object n) {
			// stop traversing when reaching a current element
			if (currentElement.contains(n) && !isAllocationSharedInContext(n, currentContext))
				return (new HashSet<Object>()).iterator();
			else
				return super.getConnected(n);
		}
	}

	public static Analysis instance;
	final CallGraph callGraph;
	final PointerAnalysis pointerAnalysis;
	public HeapGraph heapGraph;
	final PropagationCallGraphBuilder builder;
	private IClassHierarchy cha;

	public Analysis(CallGraph callGraph, PointerAnalysis pointerAnalysis, PropagationCallGraphBuilder builder) {
		this.callGraph = callGraph;
		this.pointerAnalysis = pointerAnalysis;
		this.builder = builder;
		this.heapGraph = this.pointerAnalysis.getHeapGraph();
		this.cha = this.pointerAnalysis.getClassHierarchy();
	}

	public AbstractPointerKey getLocalPointerKey(StatementWithInstructionIndex s) {
		SSAInstruction instruction = Analysis.getInstruction(s);
		if (instruction instanceof SSAPutInstruction) {
			SSAPutInstruction putI = (SSAPutInstruction) instruction;
			if (putI.isStatic()) {
				for (Object o : heapGraph) {
					if (!(o instanceof StaticFieldKey))
						continue;

					StaticFieldKey staticPK = (StaticFieldKey) o;
					putI.getDeclaredField();
					if (cha.resolveField(putI.getDeclaredField()).equals(staticPK.getField()))
						return staticPK;
				}
				throw new RuntimeException(
						"Could not find the static field pointer key in the heap graph. probably the code abobe is wrong");
			} else {
				// remained here
			}
		}
		return null; // delete this
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
		// System.out.println("Couldn't find heap Instance Key for " + cgNode
		// + "   value " + value);
		return null;
	}

	public boolean doesStatementUseInstanceKey(StatementWithInstructionIndex normalStatement, InstanceKey instanceKey) {
		int instructionIndex = normalStatement.getInstructionIndex();
		CGNode cgNode = normalStatement.getNode();
		SSAInstruction predInstruction = cgNode.getIR().getControlFlowGraph().getInstructions()[instructionIndex];
		if (predInstruction instanceof SSAPutInstruction)
			return false;
		for (int i = 0; i < predInstruction.getNumberOfUses(); i++) {
			LocalPointerKey localPointerKey = getLocalPointerKey(cgNode, predInstruction.getUse(i));

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

	public static NormalStatement getNormalStatement(AllocationSiteInNode instanceKey) {
		SSANewInstruction allocationSiteInstruction = getInstruction(instanceKey);
		int allocationSiteInstructionIndex = CodeLocation.getSSAInstructionNo(instanceKey.getNode(),
				allocationSiteInstruction);
		NormalStatement allocationSiteStatement = new NormalStatement(instanceKey.getNode(),
				allocationSiteInstructionIndex);
		return allocationSiteStatement;
	}

	public static SSANewInstruction getInstruction(AllocationSiteInNode p) {
		if (p.getNode().getIR() != null)
			return p.getNode().getIR().getNew(p.getSite());
		else
			return null;
	}

	public static SSAInstruction getInstruction(StatementWithInstructionIndex statement) {
		CGNode cgNode = statement.getNode();
		return cgNode.getIR().getInstructions()[statement.getInstructionIndex()];
	}

	public InstanceKey traceBackToShared(CGNode node, int ref) {
		if (node.getContext().equals(Everywhere.EVERYWHERE))
			return null;
		final FlexibleContext currentContext = (FlexibleContext) node.getContext();
		if (currentContext.get(ArrayContextSelector.PARALLEL) == null)
			return null;

		// the local pointer key the statement writes to
		LocalPointerKey localPointerKey = Analysis.instance.getLocalPointerKey(node, ref);
		if (localPointerKey == null)
			return null;
		// the actual instance keys for this pointer key
		int n = Analysis.instance.heapGraph.getSuccNodeCount(localPointerKey);
		Iterator<Object> instanceKeys = Analysis.instance.heapGraph.getSuccNodes(localPointerKey);

		final Set<InstanceKey> currentElement;;
		Integer elementValue = (Integer) currentContext.getItem(ArrayContextSelector.ELEMENT_VALUE);
		CGNode elementNode = (CGNode) currentContext.getItem(ArrayContextSelector.NODE);
		if (elementValue != -1) {
			LocalPointerKey elementPK = getLocalPointerKey(elementNode, elementValue);
			currentElement = getInstanceKeysForLocalValue(elementNode, elementValue);
		} else {
			CallSiteReference site = (CallSiteReference) currentContext.getItem(ArrayContextSelector.CALL_SITE_REFERENCE);
			SSAAbstractInvokeInstruction[] calls = elementNode.getIR().getCalls(site);
			currentElement = new HashSet<InstanceKey>();
			Set<InstanceKey> elementsForInvocationI = null;
			for (SSAAbstractInvokeInstruction i : calls) {
				int def = i.getDef();
				 elementsForInvocationI = getInstanceKeysForLocalValue(elementNode, def);
			}
			currentElement.addAll(elementsForInvocationI);
		}

		final Graph<Object> revertedHeap = GraphInverter.invert(Analysis.instance.heapGraph);
		@SuppressWarnings("deprecation")
		BFSPathFinder<Object> find = new PathToSharedFinder(revertedHeap, instanceKeys, currentElement, currentContext);

		List<Object> listOfSharedObjects = find.find();

		if (listOfSharedObjects == null)
			return null;
		else {
			AllocationSiteInNode sharedInstance = (AllocationSiteInNode) listOfSharedObjects.get(0);
			return sharedInstance;
		}
	}

	private Set<InstanceKey> getInstanceKeysForLocalValue(CGNode elementNode, int def) {
		Set<InstanceKey> currentElement;
		LocalPointerKey callDefKey = getLocalPointerKey(elementNode,def);
		Iterator<Object> succNodes = heapGraph.getSuccNodes(callDefKey);
		currentElement = new HashSet<InstanceKey>();
		while (succNodes.hasNext()) {
			Object object = (Object) succNodes.next();
			currentElement.add((InstanceKey) object);
		}
		return currentElement;
	}
	
	private static boolean isAllocationSharedInContext(Object iK , final FlexibleContext currentContext) {
		boolean allocationIsShared;
		AllocationSiteInNode allocationSite = (AllocationSiteInNode) iK;
		Context instanceAllocationContext = allocationSite.getNode().getContext();
		if (instanceAllocationContext.equals(Everywhere.EVERYWHERE)
				|| !currentContext.equals(instanceAllocationContext, ArrayContextSelector.ARRAY))
			allocationIsShared = true;
		else
			allocationIsShared = false;
		return allocationIsShared;
	}
}
