package edu.illinois.reLooper.sabazios;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class BarnesHutEvaluation extends DataRaceAnalysisTest{

	public BarnesHutEvaluation() {
		this.addBinaryDependency("../evaluation/Lonestar-2.1/bin/BarnesHut");
	}
	
	@Test
	public void bla() throws CancelException {
		Set<Race> races = findRaces("LBarnesHut/src/java/ParallelBarneshut", "main([Ljava/lang/String;)V");
		
		
		for (Iterator<Race> iterator = races.iterator(); iterator.hasNext();) {
			Race race = (Race) iterator.next();
			System.out.println(race);
			
		}
	}

}
