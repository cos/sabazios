package edu.illinois.reLooper.sabazios;

import java.util.Collection;

import com.ibm.wala.demandpa.alg.ContextSensitiveStateMachine;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo;
import com.ibm.wala.demandpa.util.MemoryAccessMap;
import com.ibm.wala.demandpa.util.PABasedMemoryAccessMap;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;

public class DemandDrivenRaceConfirmer {
	private final Analysis analysis;

	public DemandDrivenRaceConfirmer(Analysis analysis) {
		this.analysis = analysis;
	}
	
	public boolean confirm(LocalPointerKey localPointerKey,
			BeforeInAfterVisitor beforeInAfter) {
		MemoryAccessMap mam = new PABasedMemoryAccessMap(analysis.callGraph, analysis.pointerAnalysis);
		DemandRefinementPointsTo demandRefinementPointsTo = DemandRefinementPointsTo.makeWithDefaultFlowGraph(analysis.callGraph, analysis.pointerAnalysis.getHeapModel(), mam, 
				analysis.pointerAnalysis.getClassHierarchy(), analysis.builder.getOptions(), new ContextSensitiveStateMachine.Factory());
		
		Collection<InstanceKey> pointsTo = demandRefinementPointsTo.getPointsTo(localPointerKey);
		
		for (InstanceKey instanceKey : pointsTo) {
			if(beforeInAfter.before.contains(Analysis.getNormalStatement((AllocationSiteInNode) instanceKey)))
				return true;
		}
		return false;
	}
}
