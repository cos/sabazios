package sabazios.deref;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import sabazios.A;
import sabazios.util.Tuple;
import sabazios.util.Tuple.Pair;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.FieldReference;

public class Dereferences {

	static LinkedHashMap<Pair<CGNode, Integer>, Set<DerefRep>> cached = new LinkedHashMap<Pair<CGNode, Integer>, Set<DerefRep>>();
	static LinkedHashMap<Pair<Pair<CGNode, Integer>, Pair<CGNode, Integer>>, Set<DerefRep>> cachedWithExtraStop = new LinkedHashMap<Tuple.Pair<Pair<CGNode, Integer>, Pair<CGNode, Integer>>, Set<DerefRep>>();
	

	public static Set<DerefRep> get(A a, CGNode n, int v) {
		if (!cached.containsKey(Tuple.from(n, v))) {
			Set<DerefRep> result = Dereferences.infer(a, n, v);
			cached.put(Tuple.from(n, v), result);
		}
		return cached.get(Tuple.from(n, v));
	}

	public static Set<DerefRep> get(A a, CGNode n, int v, CGNode extraStopN, int extraStopV) {
		Pair<Pair<CGNode, Integer>, Pair<CGNode, Integer>> key = Tuple.from(Tuple.from(n, v),
				Tuple.from(extraStopN, extraStopV));
		Set<DerefRep> result = cachedWithExtraStop.get(key);
		if (result == null) {
			result = Dereferences.infer(a, n, v, extraStopN, extraStopV);
			cachedWithExtraStop.put(key, result);
		}
		return result;
	}

	/**
	 * 
	 * @param n the CGNode
	 * @param v the value
	 * @return the set of DerefRep or null if it cannot infer (ex. infinite loop, recursion, etc)
	 */
	private static Set<DerefRep> infer(A a, CGNode n, int v) {
		return infer(a, n, v, null, null);
	}

	
	/**
	 * 
	 * @param n
	 * @param v
	 * @param extraStopN
	 * @param extraStopV
	 * @return the set of DerefRep or null if it cannot infer (ex. infinite loop, recursion, etc)
	 */
	public static Set<DerefRep> infer(A a, CGNode n, int v, CGNode extraStopN, Integer extraStopV) {

		LinkedHashSet<DerefRep> w = new LinkedHashSet<DerefRep>();
		DerefRep start = new DerefRep();
		start.add(new Deref(n, v, null));
		w.add(start);

		int oter = 0;

		boolean notFinished = true;
		while (notFinished) {
			oter++;
			if (oter == 1000)
				return null;
			notFinished = false;
			LinkedHashSet<DerefRep> newW = new LinkedHashSet<DerefRep>();
			int iter = 0;
			for (DerefRep l : w) {
				iter++;
				if (iter == 1000)
					return null;
				Deref head = l.peek();
				v = head.v;
				n = head.n;

				if (n.equals(extraStopN) && v == extraStopV) {
					newW.add(l);
					continue;
				}

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
					DerefRep newL = (DerefRep) l.clone();
					Integer pred = ma.getPredNodes(v, fref).next();
					Deref newPair = new Deref(n, pred, fref);
					if (newL.contains(newPair))
						return null;
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
							Iterator<CGNode> predNodes = a.callGraph.getPredNodes(n);
							while (predNodes.hasNext()) {
								CGNode pn = (CGNode) predNodes.next();
								Iterator<CallSiteReference> possibleSites = a.callGraph.getPossibleSites(pn, n);
								while (possibleSites.hasNext()) {
									CallSiteReference callSite = (CallSiteReference) possibleSites.next();
									SSAAbstractInvokeInstruction[] calls = pn.getIR().getCalls(callSite);
									for (SSAAbstractInvokeInstruction ii : calls) {
										DerefRep newL = (DerefRep) l.clone();
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
			if (w.equals(newW))
				return w;
			w = newW;
		}
		return w;

	}

	public static boolean isStopNode(CGNode n) {
		IMethod method = n.getMethod();
		boolean matches = method.toString().matches(".*op\\([^,]*\\).*");
		return matches;
	}
}
