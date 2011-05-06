package sabazios.tests.evaluation;

import java.util.Iterator;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.raceObjects.Race;

public class OldCorefEvaluation extends DataRaceAnalysisTest {

	public OldCorefEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/coref/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		this.addJarDependency("../evaluation/coref/java_cup_runtime.jar");
		U.detailedResults = false;
	}

	@Test
	public void test() throws CancelException {		
		findCA("LLBJ2/nlp/coref/ClusterMerger", "main([Ljava/lang/String;)V");
	}
}