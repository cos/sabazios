package edu.illinois.reLooper.sabazios;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.race.Race;
import edu.illinois.reLooper.sabazios.race.RaceOnNonStatic;

public class BarnesHutEvaluation extends DataRaceAnalysisTest{

	public BarnesHutEvaluation() {
		this.setBinaryDependency("../evaluation/Lonestar-2.1/bin/BarnesHut");
		this.setBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void bla() throws CancelException {
		Set<Race> races = findRaces("LBarnesHut/src/java/ParallelBarneshut", "main([Ljava/lang/String;)V");
		System.out.println(races.size());
		System.out.println(races.iterator().next().toDetailedString(callGraph));
	}
}
