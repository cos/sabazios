package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

public class JUnitEvaluation extends DataRaceAnalysisTest{

	public JUnitEvaluation() {
		this.addBinaryDependency("../evaluation/junit/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		this.projectName = "jUnit";
	}
	
	@Test
	public void test() throws CancelException {
		Log.report(":size_LOC", "15.6k+220k"); 
		Log.report(":real_races", 0);
		Log.report(":beningn_races", 0);
		Log.report(":bugs", 0);
		Log.report(":notes", "\\footnote{improvable. races on TestResult (6) should be clearable. there is one race due to reflexion (.getMethod) that might be real. }");
		findCA("Ljunit/tests/ParallelAllTests", "main([Ljava/lang/String;)V");
	}
}
