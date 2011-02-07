package edu.illinois.reLooper.sabazios;

import java.util.Iterator;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class ParticleTest extends DataRaceAnalysisTest {

	public ParticleTest() {
		this.setBinaryDependency("subjects");
		this.setBinaryDependency("../ParallelArrayMock/bin");
	}

	@Test
	public void vacuouslyNoRace() throws CancelException {
		assertRaces(null);
	}

	@Test
	public void noRaceOnParameter() throws CancelException {
		assertRaces(null);
	}
	
	@Test
	public void noRaceOnParameterInitializedOutside() throws CancelException {
		assertRaces(null);
		System.out.println("bla");
	}
}
