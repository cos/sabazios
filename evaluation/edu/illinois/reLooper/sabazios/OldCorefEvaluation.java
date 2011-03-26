package edu.illinois.reLooper.sabazios;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class OldCorefEvaluation extends DataRaceAnalysisTest{

	public OldCorefEvaluation() {
		super();
		this.addBinaryDependency("../old/WALATests_workinprogress/bin");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void test() throws CancelException {
		foundRaces = findRaces("LLBJ2/nlp/coref/ClusterMerger", "main([Ljava/lang/String;)V");
		assertNoRaces();
	}
}