package sabazios.tests.evaluation;

import java.io.IOException;

import junit.framework.TestSuite;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import sabazios.util.Log;

@RunWith(Suite.class)
@Suite.SuiteClasses( { 
	BarnesHutEvaluation.class,
	MonteCarloEvaluation.class,
	OldEm3dEvaluation.class,
	OldCorefEvaluation.class,
	LuSearchEvaluation.class,
	WekaEvaluation.class,
	JUnitEvaluation.class
	})
public class EvaluationSuite extends TestSuite {
  @AfterClass
  public static void reportResults() {
  	try {
	    Log.outputReport("../../paper/results.tex");
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
  }
}