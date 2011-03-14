package edu.illinois.reLooper.sabazios.race;


import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;

import edu.illinois.reLooper.sabazios.Analysis;
import edu.illinois.reLooper.sabazios.CodeLocation;

public class RaceOnNonStatic extends Race {
	private final InstanceKey instanceKey;
	private final boolean isLoopCarriedDependency;

	/**
	 * Describes a race.
	 * 
	 * If instanceKey is null, this a race to a static field.
	 * 
	 * @param statement
	 * @param instanceKey
	 */
	public RaceOnNonStatic(NormalStatement statement, InstanceKey instanceKey, boolean isLoopCarriedDependency) {
		super(statement);
		this.instanceKey = instanceKey;
		this.isLoopCarriedDependency = isLoopCarriedDependency;
	}

	@Override
	public String toString() {
		AllocationSiteInNode allocationSite = (AllocationSiteInNode) instanceKey;
		CodeLocation allocationLocation;
		if (instanceKey != null)
			allocationLocation = CodeLocation.make(allocationSite.getNode(), allocationSite.getSite()
					.getProgramCounter());
		else
			allocationLocation = null;

		return super.toString() + " write to " + allocationLocation;
	}

	public String toDetailedString(CallGraph callGraph) {
		StringBuffer s = new StringBuffer();
		s.append(this);
		s.append("\n");
		s.append(this.toStackTraceString(callGraph));
		s.append("\n");
		s.append("Allocation site\n");
		s.append(this.getAllocationStackTrace(callGraph));
		s.append("\n");
		return s.toString();
	}

	public boolean isLoopCarriedDependency() {
		return isLoopCarriedDependency;
	}

	public InstanceKey getInstanceKey() {
		return instanceKey;
	}

	public String getAllocationStackTrace(CallGraph callGraph) {
		AllocationSiteInNode allocationSite = (AllocationSiteInNode) instanceKey;
		if (instanceKey == null)
			return "STATIC";
		return getStackTrace(callGraph, Analysis.getNormalStatement(allocationSite));
	}
}
