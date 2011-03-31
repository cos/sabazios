package edu.illinois.reLooper.sabazios.race;


import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.slicer.NormalStatement;

import edu.illinois.reLooper.sabazios.Analysis;
import edu.illinois.reLooper.sabazios.CodeLocation;

public class RaceOnNonStatic extends Race {
	private final InstanceKey instanceKey;

	/**
	 * Describes a race.
	 * 
	 * If instanceKey is null, this a race to a static field.
	 * 
	 * @param statement
	 * @param instanceKey
	 */
	public RaceOnNonStatic(NormalStatement statement, InstanceKey instanceKey) {
		super(statement);
		this.instanceKey = instanceKey;
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

	@Override
	public String toDetailedString(Analysis analysis) {
		StringBuffer s = new StringBuffer();
		s.append(this);
		s.append("\n");
		s.append(this.toStackTraceString(analysis));
		s.append("\n");
		s.append("Allocation site\n");
		s.append(this.getAllocationStackTrace(analysis));
		s.append("\n");
		return s.toString();
	}

	public InstanceKey getInstanceKey() {
		return instanceKey;
	}

	public String getAllocationStackTrace(Analysis analysis) {
		AllocationSiteInNode allocationSite = (AllocationSiteInNode) instanceKey;
		return instanceKey.toString() + " \n " + getStackTrace(analysis, Analysis.getNormalStatement(allocationSite));
	}
}
