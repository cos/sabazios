package sabazios.tests.play;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

public class LuSearchEvaluation extends DataRaceAnalysisTest{

	public LuSearchEvaluation() {
		super();
		this.addBinaryDependency("../evaluation-play/dacapo-9.12-bach-src/benchmarks/bms/lusearch/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		U.detailedResults = false;
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Lorg/dacapo/lusearch/Search", "main([Ljava/lang/String;)V");
	}
}