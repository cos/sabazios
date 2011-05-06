package sabazios.tests.synthetic;

import sabazios.tests.DataRaceAnalysisTest;

public class ParticleTestAdvanced extends DataRaceAnalysisTest {

	public ParticleTestAdvanced() {
		super();
		
		DEBUG = true;
		this.addBinaryDependency("synthetic");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
}
