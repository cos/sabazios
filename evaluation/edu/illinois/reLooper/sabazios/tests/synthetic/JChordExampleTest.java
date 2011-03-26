package edu.illinois.reLooper.sabazios.tests.synthetic;

import java.util.Set;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.DataRaceAnalysisTest;
import edu.illinois.reLooper.sabazios.race.Race;

public class JChordExampleTest extends DataRaceAnalysisTest {

	public JChordExampleTest() {
		super();
		DEBUG = true;
		this.addBinaryDependency("synthetic");
	}
	
	@Test
	public void test() throws CancelException {
		Set<Race> races = findRaces("Lsynthetic/jchordExample/T", "main([Ljava/lang/String;)V");
		System.out.println(races.size());
		System.out.println(races.iterator().next().toDetailedString(callGraph));
	}
}
