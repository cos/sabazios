package edu.illinois.reLooper.sabazios;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class BarnesHutEvaluation extends DataRaceAnalysisTest{

	public BarnesHutEvaluation() {
		this.setBinaryDependency("../evaluation/Lonestar-2.1/bin/BarnesHut");
		this.setBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void bla() throws CancelException {
		Set<Race> races = findRaces("LBarnesHut/src/java/ParallelBarneshut", "main([Ljava/lang/String;)V");
		
		
		printRaces(races);
	}
}
