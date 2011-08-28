package racefix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.A;
import sabazios.domains.PointerForValue;
import sabazios.util.CodeLocation;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.collections.Filter;

/**
 * 
 * @author caius
 * 
 */
@SuppressWarnings("deprecation")
public class AccessTrace {
	private final CGNode n;
	private final int v;
	private final A a;
	private final Filter<CGNode> f;
	private LinkedHashSet<PointerKey> pointers = new LinkedHashSet<PointerKey>();
	private LinkedHashSet<InstanceKey> instances = new LinkedHashSet<InstanceKey>();
	private HashMap<CGNode, Set<Integer>> visited = new HashMap<CGNode, Set<Integer>>();
	private PointerForValue pv;

	public AccessTrace(A a, CGNode n, int v) {
		this(a, n, v, null);
	}

	public AccessTrace(A a, CGNode n, int v, Filter<CGNode> f) {
		this.a = a;
		this.n = n;
		this.v = v;
		this.f = f;
		pv = a.pointerForValue;
	}

	public void compute() {
		CGNode n2 = n;
		int v2 = v;
		solveNV(n2, v2);
	}

	private void solveNV(CGNode node, int value) {
		if (f != null && !f.accepts(node))
			return;

		Set<Integer> set = visited.get(node);
		if (set == null) {
			set = new LinkedHashSet<Integer>();
			set.add(value);
			visited.put(node, set);
		} else if (set.contains(value))
			return;
		else
			set.add(value);

		LocalPointerKey lpk = pv.get(node, value);
		Iterator<Object> succNodes = a.heapGraph.getSuccNodes(lpk);
		while (succNodes.hasNext()) {
			InstanceKey o = (InstanceKey) succNodes.next();
			instances.add(o);

			if (node.getMethod().getNumberOfParameters() >= value) {
				Iterator<CGNode> predNodes = a.callGraph.getPredNodes(node);
				while (predNodes.hasNext()) {
					CGNode cgNode = (CGNode) predNodes.next();

					Iterator<CallSiteReference> possibleSites = a.callGraph
							.getPossibleSites(cgNode, node);
					while (possibleSites.hasNext()) {
						CallSiteReference callSiteReference = (CallSiteReference) possibleSites
								.next();
						SSAAbstractInvokeInstruction[] calls = cgNode.getIR()
								.getCalls(callSiteReference);

						// now we must find the calls that have as a possible
						// parameter
						// our node
						for (SSAAbstractInvokeInstruction ssaAbstractInvokeInstruction : calls) {
							int use = ssaAbstractInvokeInstruction
									.getUse(value - 1);
							solveNV(cgNode, use);
						}
					}
				}
			}

			// method invokation

			// find the def

			// SSAInvokeInstruction ssaII;
			// ssaII.getCallSite();
			// a.callGraph.getNumberOfTargets(node, site);

			// SSAReturnInstruction ssaRI;
			// ssaRI.getUse(j);

			DefUse du = node.getDU();
			SSAInstruction def = du.getDef(value);

			if (def instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction invoke = (SSAInvokeInstruction) def;
				CallSiteReference callSite = invoke.getCallSite();
				Set<CGNode> possibleTargets = a.callGraph.getPossibleTargets(
						node, callSite);
				for (CGNode cgNode : possibleTargets) {
					Iterator<SSAInstruction> instructionsIterator = cgNode
							.getIR().iterateAllInstructions();
					while (instructionsIterator.hasNext()) {
						SSAInstruction ssaInstruction = (SSAInstruction) instructionsIterator
								.next();
						if (ssaInstruction instanceof SSAReturnInstruction) {
							SSAReturnInstruction returnInstr = (SSAReturnInstruction) ssaInstruction;
							int use = returnInstr.getUse(0);
							solveNV(cgNode, use);
						}
					}
				}
			}

			if (def instanceof SSAGetInstruction) {
				SSAGetInstruction get = (SSAGetInstruction) def;
				if (!get.isStatic()) {
					addInstanceFieldKeysForInstructionInNode(node, o, get);
					solveNV(node, get.getRef());
				} else {
					addStaticFieldKeysForInstructionInNode(node, o, get);
					InstanceKey classInstanceKey = a.heapGraph.getHeapModel()
							.getInstanceKeyForClassObject(
									get.getDeclaredField().getDeclaringClass());
					instances.add(classInstanceKey);
				}
			}

			if (def instanceof SSAPhiInstruction) {
				SSAPhiInstruction phi = (SSAPhiInstruction) def;
				int numberOfUses = phi.getNumberOfUses();
				for (int i = 0; i < numberOfUses; i++) {
					int use = phi.getUse(i);
					solveNV(node, use);
				}
			}

		}
	}

	private void addStaticFieldKeysForInstructionInNode(CGNode node,
			InstanceKey o, SSAGetInstruction get) {
		Iterator<Object> predNodes = a.heapGraph.getPredNodes(o);
		while (predNodes.hasNext()) {
			PointerKey pointer = (PointerKey) predNodes.next();
			if (pointer instanceof StaticFieldKey) {
				StaticFieldKey sField = (StaticFieldKey) pointer;
				if (sField.getField().equals(
						a.cha.resolveField(get.getDeclaredField()))) {
					pointers.add(sField);
				}
			}
		}
	}

	private void addInstanceFieldKeysForInstructionInNode(CGNode node,
			InstanceKey o, SSAGetInstruction get) {
		int v1 = get.getRef(); // the ssa value for the object from which the
								// current object is obtained
		LocalPointerKey lpc = pv.get(node, v1);
		Iterator<Object> predNodes = a.heapGraph.getSuccNodes(lpc);
		Set<InstanceKey> objs = new HashSet<InstanceKey>();
		while (predNodes.hasNext()) {
			Object object = (Object) predNodes.next();
			objs.add((InstanceKey) object);
		}

		FieldReference declaredField = get.getDeclaredField();
		Iterator<Object> pred = a.heapGraph.getPredNodes(o);
		while (pred.hasNext()) {
			Object prev = (Object) pred.next();
			if (prev instanceof InstanceFieldKey) {
				InstanceFieldKey field = (InstanceFieldKey) prev;
				IField ifield = field.getField();
				if (ifield.getName().equals(declaredField.getName())
						&& a.cha.resolveField(ifield.getDeclaringClass(),
								declaredField).equals(ifield)) {
					Iterator<Object> predNodes2 = a.heapGraph
							.getPredNodes(field);
					while (predNodes2.hasNext()) {
						Object object = (Object) predNodes2.next();
						if (objs.contains(object)) {
							pointers.add(field);
							break;
						}
					}
				}
			}
		}
	}

	public String getTestString() {
		String s = "";

		for (PointerKey o : pointers) {
			if (o instanceof LocalPointerKey) {
				LocalPointerKey p = (LocalPointerKey) o;
				s += "LPK:";
				s += p.getNode().getMethod().getDeclaringClass().getName()
						.getClassName().toString();
				s += ".";
				s += p.getNode().getMethod().getName().toString();
				s += "-v";
				s += p.getValueNumber();
				String variableName = CodeLocation.variableName(n,
						p.getValueNumber());
				if (variableName != null)
					s += "-" + variableName;
				s += "\n";
			} else if (o instanceof StaticFieldKey) {
				s += "SFK:";
				s += ((StaticFieldKey) o).getField().getDeclaringClass().getName().getClassName().toString();
				s += ".";
				s += ((StaticFieldKey) o).getField().getName().toString();
				s += "\n";
			} else if (o instanceof InstanceFieldKey) {
				InstanceFieldKey p = (InstanceFieldKey) o;
				s += "IFK:";
				s += p.getField().getDeclaringClass().getName().getClassName()
						.toString();
				s += ".";
				s += p.getField().getName().toString();
				s += "\n";
			} else 
				throw new RuntimeException("PointerKey type not recognized "
						+ o.toString());
		}

		for (InstanceKey o : instances) {
			if (o instanceof AllocationSiteInNode) {
				AllocationSiteInNode as = (AllocationSiteInNode) o;
				s += "O:";
				s += as.getNode().getMethod().getDeclaringClass().getName()
						.getClassName().toString();
				s += ".";
				s += as.getNode().getMethod().getName().toString();
				s += "-new ";
				s += as.getSite().getDeclaredType().getName().getClassName()
						.toString();
				s += "\n";
			} else {
				if (o instanceof ConcreteTypeKey) {
					IClass concreteType = o.getConcreteType();
					s += "C:";
					s += concreteType.getName().getClassName().toString();
					s += "\n";
				} else if (o instanceof ConstantKey) {
					s += "C:";
					s += ((IClass) (((ConstantKey) o).getValue())).getName()
							.getClassName().toString();
					s += "\n";
				} else
					throw new RuntimeException("Instance type not recognized "
							+ o.toString());
			}
		}
		return s;
	}

	public Set<PointerKey> getPointers() {
		return pointers;
	}

	public LinkedHashSet<InstanceKey> getinstances() {
		return instances;
	}
}