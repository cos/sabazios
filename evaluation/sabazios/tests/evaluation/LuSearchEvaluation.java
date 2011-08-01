package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

public class LuSearchEvaluation extends DataRaceAnalysisTest{

	public LuSearchEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/lusearch/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		U.detailedResults = false;
		this.projectName = "LuSearch";
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Lorg/dacapo/lusearch/Search", "main([Ljava/lang/String;)V");
	}
}