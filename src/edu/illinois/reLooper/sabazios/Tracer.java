package edu.illinois.reLooper.sabazios;

import java.util.Collection;
import java.util.Collections;

import com.ibm.wala.dataflow.IFDS.PartiallyBalancedTabulationSolver;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.PDG;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.SliceProblem;
import com.ibm.wala.util.CancelException;

public class Tracer {
//	public static void computeTrace(Statement s, CallGraph cg,
//			PointerAnalysis pa, DataDependenceOptions dOptions,
//			ControlDependenceOptions cOptions) throws IllegalArgumentException,
//			CancelException {
//		trace(new SDG(cg, pa, ModRef.make(), dOptions, cOptions),
//				Collections.singleton(s), false);
//	}
//	
//	public Tracer() {
//	}
//
//	public Collection<Statement> trace(SDG sdg, Collection<Statement> roots,
//			boolean backward) throws CancelException {
//		new SDG(cg, pa, ModRef.make(), dOptions, cOptions)
//		if (sdg == null) {
//			throw new IllegalArgumentException("sdg cannot be null");
//		}
//
//		SliceProblem p = makeSliceProblem(roots, sdg, backward);
//
//		PartiallyBalancedTabulationSolver<Statement, PDG, Object> solver = PartiallyBalancedTabulationSolver
//				.createPartiallyBalancedTabulationSolver(p, null);
//		TabulationResult<Statement, PDG, Object> tr = solver.solve();
//
//		Collection<Statement> slice = tr.getSupergraphNodesReached();
//
//		if (VERBOSE) {
//			System.err.println("Slicer done.");
//		}
//
//		return slice;
//	}
}
