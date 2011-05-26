package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.util.CancelException;

public class MonteCarloEvaluation extends DataRaceAnalysisTest{

	public MonteCarloEvaluation() {
		super();
		this.addBinaryDependency("../evaluation/montecarlo/bin");
		this.addBinaryDependency("../lib/parallelArray.mock");
	}
	
	@Test
	public void test() throws CancelException {
		findCA("Lmontecarlo/parallel/JGFMonteCarloBench", "JGFrun(I)V");
		assertCAs("Loop: montecarlo.parallel.AppDemo.runParallel(AppDemo.java:178) Array: extra166y.ParallelArray.createUsingHandoff(ParallelArray.java:18)[EXTRA_CONTEXT => < Application, Lmontecarlo/parallel/AppDemo, initTasks(I)V >invokestatic < Application, Lextra166y/ParallelArray, createUsingHandoff([Ljava/lang/Object;Ljsr166y/ForkJoinPool;)Lextra166y/ParallelArray; >@7]Everywhere\n" + 
				"   Class: Lmontecarlo/parallel/Universal\n" + 
				"      Write accesses:\n" + 
				"        Write montecarlo.parallel.Universal.<init>(Universal.java:63) - .UNIVERSAL_DEBUG\n" + 
				"      Other accesses:\n" + 
				"        Write montecarlo.parallel.Universal.<init>(Universal.java:63) - .UNIVERSAL_DEBUG\n");
	}
}
