package edu.illinois.reLooper.sabazios.tests.synthetic;

import org.junit.Ignore;
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
		assertRace("synthetic.Particle$5.op(Particle.java:67)");
	}
	
	@Test 
	public void verySimpleRaceWithIndex() {
		findRaces();
		assertRace("synthetic.Particle$28.op(Particle.java:382)");
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
		assertRace("synthetic.Particle$7.op(Particle.java:91)");
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
		assertRaces("synthetic.Particle$15.op(Particle.java:231)","synthetic.Particle$15.op(Particle.java:232)");
	}
	
	@Test 
	public void raceOnSharedObjectCarriedByArray() {
		findRaces();
		assertRaces("synthetic.Particle.moveTo(Particle.java:12)","synthetic.Particle.moveTo(Particle.java:13)");
	}
	
	@Test 
	public void raceBecauseOfDirectArrayLoad() {
		findRaces();
		assertRaces("synthetic.Particle$18.op(Particle.java:274)");
	}
	
	@Test 
	public void verySimpleRaceToStatic() {
		findRaces();
		assertRace("synthetic.Particle$29.op(Particle.java:397)");
	}
	
	@Override
	protected String getTestedClassName() {
		return "Lsynthetic/Particle";
	};
	
	
	@Test 
	public void raceOnSharedReturnValue() {
		findRaces();
		assertRaces("synthetic.Particle$19.op(Particle.java:289)");
	}
	
	@Test
	public void raceOnDifferntArrayIteration() {
		findRaces();
		assertRaces("synthetic.Particle$22.op(Particle.java:316)");
	}
	
	@Test @Ignore
	public void noRaceIfFlowSensitive() {
		findRaces();
		assertNoRaces();
	}
	
	@Test
	public void raceOnDifferntArrayIterationOneLoop() {
		findRaces();
		assertRaces("synthetic.Particle$27.op(Particle.java:366)","synthetic.Particle$27.op(Particle.java:367)");
	}
	
	@Test
	public void raceOnSharedFromStatic() {
		findRaces();
		assertRaces("synthetic.Particle$30.op(Particle.java:411)");
	}
}
