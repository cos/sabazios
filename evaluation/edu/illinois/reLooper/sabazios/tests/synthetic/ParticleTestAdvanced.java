package edu.illinois.reLooper.sabazios.tests.synthetic;

import edu.illinois.reLooper.sabazios.DataRaceAnalysisTest;

public class ParticleTestAdvanced extends DataRaceAnalysisTest {

	public ParticleTestAdvanced() {
		super();
		
		DEBUG = true;
		this.addBinaryDependency("synthetic");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
}
