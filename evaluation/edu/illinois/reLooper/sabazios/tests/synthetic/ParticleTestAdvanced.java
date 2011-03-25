package edu.illinois.reLooper.sabazios.tests.synthetic;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.DataRaceAnalysisTest;

public class ParticleTestAdvanced extends DataRaceAnalysisTest {

	public ParticleTestAdvanced() {
		super();
		
		DEBUG = true;
		this.setBinaryDependency("synthetic");
		this.setBinaryDependency("../ParallelArrayMock/bin");
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
	
	@Test
	public void noRaceIfFlowSensitive() {
		findRaces();
		assertNoRaces();
	}
	
	@Test
	public void raceOnDifferntArrayIterationOneLoop() {
		findRaces();
		assertRaces("synthetic.Particle$27.op(Particle.java:366)","synthetic.Particle$27.op(Particle.java:367)");
	}
}
