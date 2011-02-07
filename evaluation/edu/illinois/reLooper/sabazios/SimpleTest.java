package edu.illinois.reLooper.sabazios;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class SimpleTest extends DataRaceAnalysisTest {

	public SimpleTest() {
		this.setBinaryDependency("subjects");
		this.setBinaryDependency("../ParallelArrayMock/bin");
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
