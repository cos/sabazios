package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;

import com.ibm.wala.util.CancelException;

public class BarnesHutEvaluation extends DataRaceAnalysisTest{

	public BarnesHutEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/barnesHut/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		this.projectName = "BH";
	}
	
	@Test
	public void test() throws CancelException {
		findCA("LbarnesHut/ParallelBarneshut", "main([Ljava/lang/String;)V");
		assertCAs("");
		Log.report(":size_LOC", "899+220k");
		Log.report(":real_races", 0);
		Log.report(":beningn_races", 0);
		Log.report(":bugs", 0);
	}
}