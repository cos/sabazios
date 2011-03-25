package edu.illinois.reLooper.sabazios;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.race.Race;
import edu.illinois.reLooper.sabazios.race.RaceOnNonStatic;

public class OldCorefEvaluation extends DataRaceAnalysisTest{

	public OldCorefEvaluation() {
		super();
		this.setBinaryDependency("../old/WALATests_workinprogress/bin");
		this.setBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void test() throws CancelException {
		foundRaces = findRaces("LLBJ2/nlp/coref/ClusterMerger", "main([Ljava/lang/String;)V");
		assertNoRaces();
	}
}