package edu.illinois.reLooper.sabazios;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

public class SimpleTest extends DataRaceAnalysisTest {

	public SimpleTest() {
		this.addBinaryDependency("subjects");
		this.addBinaryDependency("jsr166y");
		this.addBinaryDependency("extra166y");
	}
	
	@Test
	public void verySimple() throws CancelException {
		Set<Race> races = findRaces("Lsubjects/Particles", "main()V");
		
		for (Iterator<Race> iterator = races.iterator(); iterator.hasNext();) {
			Race race = (Race) iterator.next();
			System.out.println(race);
			
		}
	}
	
	@Test
	public void highCFA() throws CancelException {
		Set<Race> races = findRaces("Lsubjects/HighCFA", "main()V");
		
		for (Iterator<Race> iterator = races.iterator(); iterator.hasNext();) {
			Race race = (Race) iterator.next();
			System.out.println(race);
			
		}
	}

}
