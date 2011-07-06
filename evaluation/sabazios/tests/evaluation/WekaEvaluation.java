package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;

import com.ibm.wala.util.CancelException;

public class WekaEvaluation extends DataRaceAnalysisTest{

	public WekaEvaluation() {
		this.addJarDependency("../evaluation/weka/lib/java-cup.jar");
		this.addJarDependency("../evaluation/weka/lib/JFlex.jar");
		this.addJarDependency("../evaluation/weka/lib/junit.jar");
		this.addBinaryDependency("../evaluation/weka/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		this.projectName = "Weka";
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Lweka/clusterers/EM", "EM_Init(Lweka/core/Instances;)V");
		Log.report(":size_LOC", "301k+253k"); // javacup has 5.6k, jFlex 11k, jUnit 15.6k
		Log.report(":real_races", "27 (all)");
		Log.report(":beningn_races", "0");
		Log.report(":bugs", "2");
	}
}
