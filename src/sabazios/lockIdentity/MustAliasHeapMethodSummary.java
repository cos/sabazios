package sabazios.lockIdentity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import sabazios.util.Tuple;
import sabazios.util.Tuple.Pair;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;

public class MustAliasHeapMethodSummary extends SlowSparseNumberedLabeledGraph<ValueNode, FieldEdge> {

	public final static LinkedHashMap<IR, MustAliasHeapMethodSummary> cached = new LinkedHashMap<IR, MustAliasHeapMethodSummary>();

	public final static MustAliasHeapMethodSummary get(IR im) {
		MustAliasHeapMethodSummary result = cached.get(im);
		if (result == null) {
			result = new MustAliasHeapMethodSummary(im);
			cached.put(im, result);
		}
		return result;
	}

	private final IR ir;

	private MustAliasHeapMethodSummary(IR ir) {
		super(new FieldEdge(null, false));
		this.ir = ir;
		compute();
	}

	private void compute() {
		SSAInstruction[] instructions = ir.getInstructions();
		SymbolTable symbolTable = ir.getSymbolTable();
		this.addNode(new ValueNode(null, 0));
		for (int v = 1; v <= symbolTable.getMaxValueNumber(); v++) {
			this.addNode(new ValueNode(null, v));
		}

		for (SSAInstruction i : instructions) {
			if (i instanceof SSAPutInstruction)
				addEdge((SSAPutInstruction) i);
			if (i instanceof SSAGetInstruction)
				addEdge((SSAGetInstruction) i);
		}
	}

	private void addEdge(SSAGetInstruction i) {
		if (!i.isStatic())
			this.addEdge(this.getNode(i.getRef()), this.getNode(i.getDef()), new FieldEdge(i.getDeclaredField(), false));
		else {
			this.addEdge(this.getNode(0), this.getNode(i.getDef()), new FieldEdge(i.getDeclaredField(), false));
		}
	}

	private void addEdge(SSAPutInstruction i) {
		this.addEdge(this.getNode(i.getRef()), this.getNode(i.getVal()), new FieldEdge(i.getDeclaredField(), true));
	}
}
