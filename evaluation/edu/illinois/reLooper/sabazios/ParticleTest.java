package edu.illinois.reLooper.sabazios;

import java.util.Iterator;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class ParticleTest extends DataRaceAnalysisTest {

	public ParticleTest() {
		super();
		DEBUG = true;
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
	public void verySimpleRace() {
		assertRace("subjects.Particle$5.op(Particle.java:65)");
	}
	
	/**
	 * Is there a problem when the elements are initialized in another forall?
	 */
	@Test
	public void noRaceOnParameterInitializedBefore() throws CancelException  {
		assertNoRaces();
	}
	
	/**
	 * an part of an element is tainted in another forall
	 */
	@Test
	public void raceOnParameterInitializedBefore() throws CancelException  {
		assertRace("subjects.Particle$6.op(Particle.java:73)");
	}
	
	/**
	 * Is it field sensitive?
	 */
	@Test
	public void noRaceOnANonSharedField() throws CancelException {
		assertNoRaces();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on 0-CFA
	 * Works on 1-CFA
	 */
	@Test
	public void OneCFANeeded() throws CancelException {
		assertNoRaces();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on 0-CFA and 1-CFA
	 * Works on 2-CFA
	 */
	@Test
	public void TwoCFANeeded() throws CancelException {
		assertNoRaces();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on any CFA
	 * Might work on smarter analyses
	 */
	@Test
	public void HighCFANeeded() throws CancelException {
		assertNoRaces();
	}
}
