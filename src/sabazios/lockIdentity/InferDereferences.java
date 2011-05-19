package sabazios.lockIdentity;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.FieldReference;

import sabazios.RaceAnalysis;
import sabazios.util.Tuple;
import sabazios.util.Tuple.Pair;

public class InferDereferences {

	public static class Deref {
		public final CGNode n;
		public final Integer v;
		public final FieldReference f;

		public Deref(CGNode n, Integer v, FieldReference f) {
			this.n = n;
			this.v = v;
			this.f = f;
		}
	}

	private final CGNode n;
	private final int v;

	public InferDereferences(CGNode n, int v) {
		this.n = n;
		this.v = v;
		w = new LinkedHashSet<ArrayDeque<Deref>>();
		ArrayDeque<Deref> start = new ArrayDeque<Deref>();
		start.add(new Deref(n,v, null));
		w.add(start);
	}

	LinkedHashSet<ArrayDeque<Deref>> w;

	public LinkedHashSet<ArrayDeque<Deref>> infer() {
		MustAliasHeapMethodSummary ma = MustAliasHeapMethodSummary.get(n.getIR());
		RaceAnalysis.dotGraph(ma, "mustAlias", null);
		boolean notFinished = true;
		while (notFinished) {
			notFinished = false;
			LinkedHashSet<ArrayDeque<Deref>> newW = new LinkedHashSet<ArrayDeque<Deref>>();
			for (ArrayDeque<Deref> l : w) {
				Deref head = l.peek();
				Integer node = head.v;
				Iterator<? extends FieldReference> predLabels = ma.getPredLabels(node);
				boolean processed = false;
				while (predLabels.hasNext()) {
					FieldReference FieldReference = (FieldReference) predLabels.next();
					ArrayDeque<Deref> newL = l.clone();
					Integer pred = ma.getPredNodes(node, FieldReference).next();
					Deref newPair = new Deref(n, pred, FieldReference);
					if (newL.contains(newPair))
						throw new RuntimeException("not possible");
					newL.push(newPair);
					newW.add(newL);
					processed = true;
				}
				if (processed)
					notFinished = true;
				else
					newW.add(l);
			}
			w = newW;
		}
		return w;
	}
}
