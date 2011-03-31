package edu.illinois.reLooper.sabazios;

import java.util.Iterator;

import org.junit.Test;

import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.race.Race;

public class OldCorefEvaluation extends DataRaceAnalysisTest {

	public OldCorefEvaluation() {
		super();
		this.addBinaryDependency("../old/WALATests_workinprogress/bin");
		this.addBinaryDependency("../ParallelArrayMock/bin");
	}

	@Test
	public void test() throws CancelException {
		foundRaces = findRaces("LLBJ2/nlp/coref/ClusterMerger", "main([Ljava/lang/String;)V");
		System.out.println("Found " + foundRaces.size() + " races");
		int i = 0;
		Iterator<Race> iterator = foundRaces.iterator();
		while (i < foundRaces.size()) {
			i++;
			Race r = iterator.next();
			System.out.println(r.toDetailedString(analysis));
		}

	}
}