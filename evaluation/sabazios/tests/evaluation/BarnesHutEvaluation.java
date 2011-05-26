package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

public class BarnesHutEvaluation extends DataRaceAnalysisTest{

	public BarnesHutEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/barnesHut/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
	}
	
	@Test
	public void test() throws CancelException {
		findCA("LbarnesHut/ParallelBarneshut", "main([Ljava/lang/String;)V");
		assertCAs("");
	}
}