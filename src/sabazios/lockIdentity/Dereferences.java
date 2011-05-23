package sabazios.lockIdentity;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import sabazios.A;
import sabazios.lockIdentity.Dereferences.DerefDeque;
import sabazios.util.CodeLocation;
import sabazios.util.Tuple;
import sabazios.util.Tuple.Pair;
import sabazios.util.U;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.FieldReference;

public class Dereferences {

	public static class Deref {
		public final CGNode n;
		public final Integer v;
		public final FieldReference f;

		public Deref(CGNode n, Integer v, FieldReference f) {
			this.n = n;
			this.v = v;
			this.f = f;
		}

		@Override
		public String toString() {
			String variableName = CodeLocation.variableName(v, n, n.getIR().getInstructions().length - 1);
			variableName = "(" + variableName + ")";
			return this.n.getMethod().getName().toString() + "#v" + this.v + variableName + U.tos(f);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null)
				return false;
			if(obj.getClass() != this.getClass())
				return false;
			
			Deref other = (Deref) obj;
			return this.n.equals(other.n) && this.v == other.v && (this.f == null ? other.f == null : this.f.equals(other.f));
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(n).append(v).append(f).hashCode();
		}
	}
	
	
	static LinkedHashMap<Pair<CGNode, Integer>, Set<DerefDeque>> cached = new LinkedHashMap<Pair<CGNode,Integer>, Set<DerefDeque>>(); 
	
	public static Set<DerefDeque> get(CGNode n, int v) {
		Set<DerefDeque> result = cached.get(Tuple.from(n, v));
		if(result == null) {
			result = Dereferences.infer(n, v);
			cached.put(Tuple.from(n, v), result);
		}
		return result;
	}

	public static class DerefDeque extends ArrayDeque<Deref> {
		private static final long serialVersionUID = 5575770507179190896L;

		@Override
		public boolean equals(Object obj) {
			if(obj == null)
				return false;
			if(obj.getClass() != this.getClass())
				return false;
			
			DerefDeque other = (DerefDeque) obj;
			return Arrays.equals(this.toArray(), other.toArray());
		}
		
		@Override
		public int hashCode() {
			return Arrays.hashCode(this.toArray());
		}
	}

	public static LinkedHashSet<DerefDeque> infer(CGNode n, int v) {
		LinkedHashSet<DerefDeque> w = new LinkedHashSet<DerefDeque>();
		DerefDeque start = new DerefDeque();
		start.add(new Deref(n, v, null));
		w.add(start);
		
		boolean notFinished = true;
		while (notFinished) {
			notFinished = false;
			LinkedHashSet<DerefDeque> newW = new LinkedHashSet<DerefDeque>();
			for (DerefDeque l : w) {
				Deref head = l.peek();
				v = head.v;
				n = head.n;
				FieldReference f = head.f;

				// handle x = y.f
				MustAliasHeapMethodSummary ma = MustAliasHeapMethodSummary.get(n.getIR());
				Iterator<? extends FieldReference> predLabels = ma.getPredLabels(v);
				boolean processed = false;
				// this is just one iteration as long as we only handle reads,
				// can become more and more complicated as (if) we move to a
				// more complicated analysis
				while (predLabels.hasNext()) {
					FieldReference fref = (FieldReference) predLabels.next();
					DerefDeque newL = (DerefDeque) l.clone();
					Integer pred = ma.getPredNodes(v, fref).next();
					Deref newPair = new Deref(n, pred, fref);
					if (newL.contains(newPair))
						throw new RuntimeException("not possible");
					newL.push(newPair);
					newW.add(newL);
					processed = true;
				}
				// handle x is param
				if (!isStopNode(n)) {
					int[] parameterValueNumbers = n.getIR().getParameterValueNumbers();
					for (int pv : parameterValueNumbers) {
						if (v == pv) {
							processed = true;
							Iterator<CGNode> predNodes = A.callGraph.getPredNodes(n);
							while (predNodes.hasNext()) {
								CGNode pn = (CGNode) predNodes.next();
								Iterator<CallSiteReference> possibleSites = A.callGraph.getPossibleSites(pn, n);
								while (possibleSites.hasNext()) {
									CallSiteReference callSite = (CallSiteReference) possibleSites.next();
									SSAAbstractInvokeInstruction[] calls = pn.getIR().getCalls(callSite);
									for (SSAAbstractInvokeInstruction ii : calls) {
										DerefDeque newL = (DerefDeque) l.clone();
										Deref deref = new Deref(pn, ii.getUse(pv - 1), f);
										newL.pop();
										newL.push(deref);
										newW.add(newL);
									}
								}
							}
						}
					}
				}

				if (processed)
					notFinished = true;
				else
					newW.add(l);
			}
			if(w.equals(newW))
				return w;
			w = newW;
		}
		return w;
	}

	private static boolean isStopNode(CGNode n) {
		return n.getMethod().toString().matches(".*replaceWithGeneratedValue.*");
	}
}
