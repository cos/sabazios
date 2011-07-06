package sabazios.tests.play;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

public class OldLBJPOSEvaluation extends DataRaceAnalysisTest{

	public OldLBJPOSEvaluation() {
		this.addJarDependency("../evaluation-play/LBJPOS/lib/LBJ2.jar");
		this.addJarDependency("../evaluation-play/LBJPOS/lib/LBJ2Library.jar");
		this.addJarDependency("../evaluation-play/LBJPOS/lib/LBJPOS.jar");		
		this.addBinaryDependency("../evaluation-play/LBJPOS/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");		
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Ledu/illinois/cs/cogcomp/lbj/pos/POSTag", "main([Ljava/lang/String;)V");
	}
}
