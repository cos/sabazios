package sabazios.tests.play;

import java.util.Set;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.raceObjects.Race;

public class SimpleTest extends DataRaceAnalysisTest {

	public SimpleTest() {
		this.addBinaryDependency("subjects");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void verySimple() throws CancelException {
		Set<Race> races = findRaces("Lsubjects/Particles", "main()V");
		
		printDetailedRaces(races);
	}

	@Test
	public void highCFA() throws CancelException {
		Set<Race> races = findRaces("Lsubjects/HighCFA", "main()V");
		
		printDetailedRaces(races);
	}

}
