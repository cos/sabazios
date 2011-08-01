package sabazios.tests;

import java.io.IOException;

import junit.framework.TestSuite;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import sabazios.tests.evaluation.BarnesHutEvaluation;
import sabazios.tests.evaluation.JUnitEvaluation;
import sabazios.tests.evaluation.LuSearchEvaluation;
import sabazios.tests.evaluation.MonteCarloEvaluation;
import sabazios.tests.evaluation.OldCorefEvaluation;
import sabazios.tests.evaluation.OldEm3dEvaluation;
import sabazios.tests.evaluation.WekaEvaluation;
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
	    Log.outputReport("../../paper/results_dynamic.rb");
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
  }
}