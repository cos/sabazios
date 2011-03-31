package edu.illinois.reLooper.sabazios;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.race.Race;

public class MonteCarloEvaluation extends DataRaceAnalysisTest{

	public MonteCarloEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/monteCarlo/bin");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void test() throws CancelException {
		foundRaces = findRaces("Lmontecarlo/JGFMonteCarloBench", "JGFrun(I)V");
		
		for (Race r : foundRaces) {
			System.out.println(r.toDetailedString(analysis));
		}
//		assertNoRaces();
	}
}
