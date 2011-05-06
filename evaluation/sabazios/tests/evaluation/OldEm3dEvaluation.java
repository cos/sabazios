package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.CS;

public class OldEm3dEvaluation extends DataRaceAnalysisTest{

	public OldEm3dEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/em3d/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		CS.NCFA = 1;
	}
	
	@Test
	public void test() throws CancelException {
		foundRaces = findRaces("Lem3d/parallelArray/Em3d", "main([Ljava/lang/String;)V");
		detailedPrintRaces();
		findCA("Lem3d/parallelArray/Em3d", "main([Ljava/lang/String;)V");
	}
}