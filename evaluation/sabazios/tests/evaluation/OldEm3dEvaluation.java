package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.wala.CS;

import com.ibm.wala.util.CancelException;


public class OldEm3dEvaluation extends DataRaceAnalysisTest{

	public OldEm3dEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/em3d/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		CS.NCFA = 1;
		this.projectName = "EM3D";
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Lem3d/parallelArray/Em3d", "main([Ljava/lang/String;)V");
		assertCAs("");
		Log.report(":size_LOC", "181+220k");
		Log.report(":real_races", 0);
		Log.report(":beningn_races", 0);
		Log.report(":bugs", 0);
	}
}