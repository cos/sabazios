package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.raceObjects.Race;

public class MonteCarloEvaluation extends DataRaceAnalysisTest{

	public MonteCarloEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/montecarlo/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Lmontecarlo/parallel/JGFMonteCarloBench", "JGFrun(I)V");
	}
}
