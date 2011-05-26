package sabazios.deref;

import java.util.LinkedHashMap;

import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import com.ibm.wala.util.strings.Atom;

class MustAliasHeapMethodSummary extends SlowSparseNumberedLabeledGraph<Integer, FieldReference> {

	public final static LinkedHashMap<IR, MustAliasHeapMethodSummary> cached = new LinkedHashMap<IR, MustAliasHeapMethodSummary>();

	public final static MustAliasHeapMethodSummary get(IR im) {
		MustAliasHeapMethodSummary result = cached.get(im);
		if (result == null) {
			result = new MustAliasHeapMethodSummary(im);
			cached.put(im, result);
		}
//		RaceAnalysis.dotGraph(result, "mustAlias_"+im.getMethod().getName(), null);
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
