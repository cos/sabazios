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
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import com.ibm.wala.util.strings.Atom;

public class MustAliasHeapMethodSummary extends SlowSparseNumberedLabeledGraph<Integer, FieldReference> {

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
		super(FieldReference.findOrCreate(TypeReference.Unknown, Atom.findOrCreateAsciiAtom("unknown"), TypeReference.Unknown));
		this.ir = ir;
		compute();
	}

	private void compute() {
		SSAInstruction[] instructions = ir.getInstructions();
		SymbolTable symbolTable = ir.getSymbolTable();
		for (int v = 0; v <= symbolTable.getMaxValueNumber(); v++) 
			this.addNode(v);
		

		for (SSAInstruction i : instructions) {
			if (i instanceof SSAGetInstruction)
				addEdge((SSAGetInstruction) i);
		}
	}

	private void addEdge(SSAGetInstruction i) {
		if (!i.isStatic())
			this.addEdge(i.getRef(), i.getDef(), i.getDeclaredField());
		else {
			this.addEdge(0, i.getDef(), i.getDeclaredField());
		}
	}
}