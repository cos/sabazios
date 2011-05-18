package sabazios.lockIdentity;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.ibm.wala.ipa.callgraph.CGNode;

import sabazios.RaceAnalysis;
import sabazios.util.Tuple;
import sabazios.util.Tuple.Pair;

public class InferDereferences {
	
	static class Deref {
		public final ValueNode v;
		public final FieldEdge f;
		public final CGNode n;
		
		public Deref(CGNode n, ValueNode v, FieldEdge f) {
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
		w = new LinkedHashSet<ArrayDeque<Pair<ValueNode, FieldEdge>>>();
		ArrayDeque<Tuple.Pair<ValueNode, FieldEdge>> start = new ArrayDeque<Tuple.Pair<ValueNode, FieldEdge>>();
		start.add(new Pair<ValueNode, FieldEdge>(new ValueNode(n, v), null));
		w.add(start);
	}
	
	LinkedHashSet<ArrayDeque<Pair<ValueNode, FieldEdge>>> w;
	public LinkedHashSet<ArrayDeque<Pair<ValueNode, FieldEdge>>> infer() {
		MustAliasHeapMethodSummary ma = MustAliasHeapMethodSummary.get(n.getIR());
		RaceAnalysis.dotGraph(ma, "mustAlias", null);
		boolean notFinished = true;
		while (notFinished) {
			notFinished = false;
			LinkedHashSet<ArrayDeque<Pair<ValueNode, FieldEdge>>> newW = new LinkedHashSet<ArrayDeque<Pair<ValueNode, FieldEdge>>>();
			for (ArrayDeque<Pair<ValueNode, FieldEdge>> l : w) {
				Pair<ValueNode, FieldEdge> head = l.peek();
				ValueNode node = ma.getNode(head.p1().v);
				Iterator<? extends FieldEdge> predLabels = ma.getPredLabels(node);
				boolean processed = false;
				while (predLabels.hasNext()) {
					FieldEdge fieldEdge = (FieldEdge) predLabels.next();
					if (!fieldEdge.isW) {
						ArrayDeque<Pair<ValueNode, FieldEdge>> newL = l.clone();
						ValueNode pred = ma.getPredNodes(node, fieldEdge).next();
						Pair<ValueNode, FieldEdge> newPair = new Pair<ValueNode, FieldEdge>(pred, fieldEdge);
						if (newL.contains(newPair))
							return null;
						newL.push(newPair);
						newW.add(newL);
						processed = true;
					}
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
