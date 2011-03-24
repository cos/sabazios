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
		assertRaces("bla");
	}
	
	@Test
	public void raceOnDifferntArrayIteration() {
		findRaces();
		assertRaces("bla");
	}
}
