package edu.illinois.reLooper.sabazios;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.util.CancelException;

public class LBJPOSEvaluation extends DataRaceAnalysisTest{

	public LBJPOSEvaluation() {
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJ2.jar");
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJ2Library.jar");
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJPOS.jar");		
		this.setBinaryDependency("../evaluation/LBJPOS/bin");
		this.setBinaryDependency("../ParallelArrayMock/bin");		
	}
	
	@Test
	public void bla() throws CancelException {
		Set<Race> races = findRaces("Ledu/illinois/cs/cogcomp/lbj/pos/POSTag", "main([Ljava/lang/String;)V");
		
		System.out.println(races.iterator().next().toDetailedString(callGraph));
	}

}
