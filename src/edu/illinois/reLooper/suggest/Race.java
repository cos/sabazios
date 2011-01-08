package edu.illinois.reLooper.suggest;

import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;

public class Race {
	private final NormalStatement statement;
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
	public Race(NormalStatement statement, InstanceKey instanceKey, boolean isLoopCarriedDependency) {
		this.statement = statement;
		this.instanceKey = instanceKey;
		this.isLoopCarriedDependency = isLoopCarriedDependency;
	}

	@Override
	public String toString() {
		return (isStatic()?"STATIC ":"")+"RACE "+(isLoopCarriedDependency?" with LCD ":"")+CodeLocation.make(getStatement())+" : "+ getStatement()
				+ " write to " + getInstanceKey();
	}
	
	public boolean isStatic() {
		return getInstanceKey() == null;
	}

	public boolean isLoopCarriedDependency() {
		return isLoopCarriedDependency;
	}

	public NormalStatement getStatement() {
		return statement;
	}

	public InstanceKey getInstanceKey() {
		return instanceKey;
	}
}
