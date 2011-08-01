package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

public class JMolEvaluation extends DataRaceAnalysisTest {

	public JMolEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/jmol/bin");
	    this.addBinaryDependency("../lib/parallelArray.mock");
	    this.addJarFolderDependency("../evaluation/jmol/lib");
		U.detailedResults = false;
		this.projectName = "JMol";
	}

	@Test
	public void test() throws CancelException {
		findCA("Lorg/openscience/jmol/app/Jmol", MAIN_METHOD);
		Log.report(":size_LOC", "?"); 
		Log.report(":real_races", "?");
		Log.report(":beningn_races", "?");
		Log.report(":bugs", "?");
		Log.report(":notes", "");
	}
}