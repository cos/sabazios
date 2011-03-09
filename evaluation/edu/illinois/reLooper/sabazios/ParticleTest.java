package edu.illinois.reLooper.sabazios;

import java.util.Iterator;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class ParticleTest extends DataRaceAnalysisTest {

	public ParticleTest() {
		super();
		this.setBinaryDependency("subjects");
		this.setBinaryDependency("../ParallelArrayMock/bin");
	}

	@Test
	public void vacuouslyNoRace() throws CancelException {
		assertNoRaces();
	}

	@Test
	public void noRaceOnParameter() throws CancelException {
		assertNoRaces();
	}
	
	@Test
	public void noRaceOnParameterInitializedBefore() throws CancelException {
		assertNoRaces();
	}
	
	@Test
	public void verySimpleRace() {
		assertRace("subjects.Particle$5.op(Particle.java:65)");
	}
	
	@Test
	public void raceOnParameterInitializedBefore() {
		assertRace("subjects.Particle$6.op(Particle.java:73)");
	}
}
