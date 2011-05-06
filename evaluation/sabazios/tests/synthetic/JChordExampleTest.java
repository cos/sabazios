package sabazios.tests.synthetic;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;


public class JChordExampleTest extends DataRaceAnalysisTest {

	public JChordExampleTest() {
		super();
		DEBUG = true;
		this.addBinaryDependency("synthetic");
	}
	
	@Test
	public void test() throws CancelException {
//		Set<Race> races = findRaces("Lsynthetic/jchordExample/T", "main([Ljava/lang/String;)V");
	}
}
