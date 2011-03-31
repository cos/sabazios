package edu.illinois.reLooper.sabazios.race;

import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.slicer.NormalStatement;

import edu.illinois.reLooper.sabazios.Analysis;
import edu.illinois.reLooper.sabazios.CodeLocation;

public class ShallowRace extends Race {
	private final Race deepRace;

	public ShallowRace(NormalStatement statement, Race deepRace) {
		super(statement);
		this.deepRace = deepRace;
	}

	@Override
	public String toString() {
		String string = super.toString() + " \n  inner race: " + deepRace.toString();
		if (deepRace instanceof RaceOnNonStatic) {
			if(!(((RaceOnNonStatic) deepRace).getInstanceKey() instanceof AllocationSiteInNode))
				return string;
			AllocationSiteInNode instanceKey = (AllocationSiteInNode) ((RaceOnNonStatic) deepRace).getInstanceKey();
			string += CodeLocation.make(instanceKey.getNode(), instanceKey.getSite().getProgramCounter());
		}
		return string;
	}
	
	@Override
	public String toDetailedString(Analysis analysis) {
		return this.toString() + deepRace.toDetailedString(analysis);
	}
}
