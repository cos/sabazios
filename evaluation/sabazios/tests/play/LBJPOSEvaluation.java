package sabazios.tests.play;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

public class LBJPOSEvaluation extends DataRaceAnalysisTest{

	public LBJPOSEvaluation() {
		this.addBinaryDependency("../old/WALATests_workinprogress/LBJPOS/lib/LBJ2.jar");
		this.addBinaryDependency("../lib/parallelArray.mock");		
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Ledu/illinois/cs/cogcomp/lbj/pos/POSTag", "main([Ljava/lang/String;)V");
	}
}
