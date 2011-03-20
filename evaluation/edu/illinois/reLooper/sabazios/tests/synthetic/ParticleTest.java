package edu.illinois.reLooper.sabazios.tests.synthetic;

import java.util.Iterator;

import org.junit.Test;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.DataRaceAnalysisTest;
import edu.illinois.reLooper.sabazios.OpSelector;
import extra166y.ParallelArray;

public class ParticleTest extends DataRaceAnalysisTest {

	public ParticleTest() {
		super();
		DEBUG = true;
		this.setBinaryDependency("synthetic");
		this.setBinaryDependency("../ParallelArrayMock/bin");
		
		this.opSelector = new OpSelector() {
			@Override
			public boolean accepts(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
				String string = site.getDeclaredTarget().toString()+caller.getMethod().toString();
				return string.contains(ParallelArray.OP_STRING);
			}
		};
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
		assertRace("synthetic.Particle$5.op(Particle.java:65)");
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
		assertRace("synthetic.Particle$6.op(Particle.java:73)");
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
	 * Fails on any CFA due to recursivity
	 * Might work on smarter analyses
	 */
	@Test
	public void recursive() throws CancelException {
		assertNoRaces();
	}
	
	/**
	 * Disambiguate the trace for a race.
	 * The trace should contain "shared.moveTo(5, 7);" but not
	 * "particle.moveTo(2, 3);"
	 */
	@Test
	public void disambiguateFalseRace() throws CancelException {
		assertRaces("synthetic.Particle.moveTo(Particle.java:12)","synthetic.Particle.moveTo(Particle.java:13)");
	}
}
