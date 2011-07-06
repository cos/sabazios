package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;

import com.ibm.wala.util.CancelException;

public class MonteCarloEvaluation extends DataRaceAnalysisTest{

	public MonteCarloEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/montecarlo/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
		this.projectName = "MonteCarlo";
	}
	
	@Test
	public void test() throws CancelException {
		String entryClass = "Lmontecarlo/parallel/JGFMonteCarloBench";
		String entryMethod = "JGFrun(I)V";
		findCA(entryClass, entryMethod);
		Log.report(":size_LOC", "1441+220k");
		Log.report(":real_races", 1);
		Log.report(":beningn_races", 1);
		Log.report(":bugs", 0);
		assertCAs("Loop: montecarlo.parallel.AppDemo.runParallel(AppDemo.java:178)\n" + 
				"   Class: Lmontecarlo/parallel/Universal\n" + 
				"      Alpha accesses:\n" + 
				"        Write montecarlo.parallel.Universal.<init>(Universal.java:63) - .UNIVERSAL_DEBUG\n" + 
				"      Beta accesses:\n" + 
				"        Write montecarlo.parallel.Universal.<init>(Universal.java:63) - .UNIVERSAL_DEBUG\n");
	}
}
