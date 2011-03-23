package edu.illinois.reLooper.sabazios.tests.synthetic;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.DataRaceAnalysisTest;
import edu.illinois.reLooper.sabazios.OpSelector;
import edu.illinois.reLooper.sabazios.race.Race;
import extra166y.ParallelArray;

public class JChordExampleTest extends DataRaceAnalysisTest {

	public JChordExampleTest() {
		super();
		DEBUG = true;
		this.setBinaryDependency("synthetic");
	}
	
	@Test
	public void test() throws CancelException {
		Set<Race> races = findRaces("Lsynthetic/jchordExample/T", "main([Ljava/lang/String;)V");
		System.out.println(races.size());
		System.out.println(races.iterator().next().toDetailedString(callGraph));
	}
}
