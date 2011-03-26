package edu.illinois.reLooper.sabazios;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class BarnesHutEvaluation extends DataRaceAnalysisTest{

	public BarnesHutEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/Lonestar-2.1/bin/BarnesHut");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void test() throws CancelException {
		foundRaces = findRaces("LBarnesHut/src/java/ParallelBarneshut", "main([Ljava/lang/String;)V");
		assertNoRaces();
	}
}
