package edu.illinois.reLooper.sabazios.raceObjects;

import com.ibm.wala.ipa.slicer.NormalStatement;

public class RaceOnStatic extends Race {

	public RaceOnStatic(NormalStatement statement) {
		super(statement);
	}
	
	@Override
	public String toString() {
		return "STATIC" + super.toString();
	}
}
