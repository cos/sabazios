package edu.illinois.reLooper.sabazios;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.util.CancelException;

public class LBJPOSEvaluation extends DataRaceAnalysisTest{

	public LBJPOSEvaluation() {
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJ2.jar");
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJ2Library.jar");
		this.addJarDependency("../evaluation/LBJPOS/lib/LBJPOS.jar");		
		this.addBinaryDependency("../evaluation/LBJPOS/bin");
	}
	
	@Test
	public void bla() throws CancelException {
		Set<Race> races = findRaces("Ledu/illinois/cs/cogcomp/lbj/pos/POSTag", "main([Ljava/lang/String;)V");
		
		for (Iterator<Race> iterator = races.iterator(); iterator.hasNext();) {
			Race race = (Race) iterator.next();
			System.out.println(race);
		}
	}

}
