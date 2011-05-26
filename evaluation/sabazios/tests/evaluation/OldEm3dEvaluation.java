package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.wala.CS;

import com.ibm.wala.util.CancelException;


public class OldEm3dEvaluation extends DataRaceAnalysisTest{

	public OldEm3dEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/em3d/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		CS.NCFA = 1;
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Lem3d/parallelArray/Em3d", "main([Ljava/lang/String;)V");
		assertCAs("");
	}
}