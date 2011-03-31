package edu.illinois.reLooper.sabazios;

import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.race.Race;

public class LBJPOSEvaluation extends DataRaceAnalysisTest{

	public LBJPOSEvaluation() {
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJ2.jar");
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJ2Library.jar");
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJPOS.jar");		
		this.addBinaryDependency("../evaluation/LBJPOS/bin");
		this.addBinaryDependency("../ParallelArrayMock/bin");		
	}
	
	@Test
	public void bla() throws CancelException {
		Set<Race> races = findRaces("Ledu/illinois/cs/cogcomp/lbj/pos/POSTag", "main([Ljava/lang/String;)V");
		
		System.out.println(races.size());
		System.out.println(races.iterator().next().toDetailedString(analysis));
	}

}
