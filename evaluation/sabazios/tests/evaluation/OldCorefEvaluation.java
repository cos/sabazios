package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

public class OldCorefEvaluation extends DataRaceAnalysisTest {

	public OldCorefEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/coref/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		this.addJarDependency("../evaluation/coref/java_cup_runtime.jar");
		U.detailedResults = false;
		this.projectName = "Coref";
	}

	@Test
	public void test() throws CancelException {
		findCA("LLBJ2/nlp/coref/ClusterMerger", "main([Ljava/lang/String;)V");
		Log.report(":size_LOC", "41k+225k"); // javacup has 5.6k 
		Log.report(":real_races", "102");
		Log.report(":beningn_races", "0");
		Log.report(":bugs", "1");
		Log.report(":notes", "\\footnote{all but one of the races are real and due to a static field " +
				". fixable by making the static field thread local. " +
				"1 report on a correctly (probably) synchronized SAXParser initialization.}");
	}
}