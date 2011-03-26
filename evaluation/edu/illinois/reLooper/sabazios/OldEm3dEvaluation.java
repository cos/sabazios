package edu.illinois.reLooper.sabazios;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class OldEm3dEvaluation extends DataRaceAnalysisTest{

	public OldEm3dEvaluation() {
		super();
		this.addBinaryDependency("../old/WALATests_workinprogress/bin");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void test() throws CancelException {
		foundRaces = findRaces("Lem3d/parallelArray/Em3d", "main([Ljava/lang/String;)V");
		assertNoRaces();
	}
}
