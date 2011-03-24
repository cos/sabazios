package edu.illinois.reLooper.sabazios.tests.synthetic;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.DataRaceAnalysisTest;

public class ParticleTest extends DataRaceAnalysisTest {

	public ParticleTest() {
		super();
		
		DEBUG = true;
		this.setBinaryDependency("synthetic");
		this.setBinaryDependency("../ParallelArrayMock/bin");
	}
	
	@Test
	public void vacuouslyNoRace() throws CancelException {
		findRaces();
		assertNoRaces();
	}

	@Test
	public void noRaceOnParameter() throws CancelException {
		findRaces();
		assertNoRaces();
	}

	@Test
	public void verySimpleRace() {
		findRaces();
		assertRace("synthetic.Particle$5.op(Particle.java:63)");
	}
	
	/**
	 * Is there a problem when the elements are initialized in another forall?
	 */
	@Test
	public void noRaceOnParameterInitializedBefore() throws CancelException  {
		findRaces();
		assertNoRaces();
	}
	
	/**
	 * an part of an element is tainted in another forall
	 */
	@Test
	public void raceOnParameterInitializedBefore() throws CancelException  {
		findRaces();
		assertRace("synthetic.Particle$7.op(Particle.java:86)");
	}
	
	/**
	 * Is it field sensitive?
	 */
	@Test
	public void noRaceOnANonSharedField() throws CancelException {
		findRaces();
		assertNoRaces();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on 0-CFA
	 * Works on 1-CFA
	 */
	@Test
	public void OneCFANeeded() throws CancelException {
		findRaces();
		assertNoRaces();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on 0-CFA and 1-CFA
	 * Works on 2-CFA
	 */
	@Test
	public void TwoCFANeeded() throws CancelException {
		findRaces();
		assertNoRaces();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on any CFA due to recursivity
	 * Might work on smarter analyses
	 */
	@Test
	public void recursive() throws CancelException {
		findRaces();
		assertNoRaces();
	}
	
	/**
	 * Disambiguate the trace for a race.
	 * The trace should contain "shared.moveTo(5, 7);" but not
	 * "particle.moveTo(2, 3);"
	 */
	@Test
	public void disambiguateFalseRace() throws CancelException {
		findRaces();
		assertRaces("synthetic.Particle.moveTo(Particle.java:12)","synthetic.Particle.moveTo(Particle.java:13)");
	}
	
	@Test
	public void ignoreFalseRacesInSeqOp() {
		findRaces();
		assertNoRaces();
	}
	
	@Test
	public void raceBecauseOfOutsideInterference()  {
		findRaces();
		assertRaces("synthetic.Particle$15.op(Particle.java:219)","synthetic.Particle$15.op(Particle.java:220)");
	}
	
	@Test
	public void raceOnSharedObjectCarriedByArray() {
		findRaces();
		assertRaces("synthetic.Particle.moveTo(Particle.java:12)","synthetic.Particle.moveTo(Particle.java:13)");
	}
	
	@Test
	public void raceBecauseOfDirectArrayLoad() {
		findRaces();
		assertRaces("synthetic.Particle$18.op(Particle.java:263)");
	}
}
