package sabazios.tests.play;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

public class SimpleTest extends DataRaceAnalysisTest {

	public SimpleTest() {
		this.addBinaryDependency("subjects");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void verySimple() throws CancelException {
//		Set<Race> races = findRaces("Lsubjects/Particles", "main()V");
		
	
	}

	@Test
	public void highCFA() throws CancelException {
//		Set<Race> races = findRaces("Lsubjects/HighCFA", "main()V");
		
	}

}
