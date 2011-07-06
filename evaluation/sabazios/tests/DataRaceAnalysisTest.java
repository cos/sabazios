package sabazios.tests;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import junit.framework.Assert;
import sabazios.A;
import sabazios.domains.ConcurrentAccesses;
import sabazios.util.InstructionsGatherer;
import sabazios.util.Log;
import sabazios.wala.WalaAnalysis;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

public abstract class DataRaceAnalysisTest extends WalaAnalysis {

	// what files/classes to include?
	// what are the entrypoints?

	protected String testClassName;
	protected static boolean DEBUG = false;
	protected ConcurrentAccesses<?> foundCA;
	
	@Rule
	public TestName name = new TestName();
	protected String projectName;

	public DataRaceAnalysisTest() {
		Log.start();
		testClassName = this.getClass().getName();
	}
	
	@Before 
	public void setCurrentProjectForLog() {
		Log.setCurrentTestProject(this.projectName);
	}

	public ConcurrentAccesses<?> findCA(String entryClass, String entryMethod) {

		try {
			if (pointerAnalysis == null)
				setup(entryClass, entryMethod);
		} catch (ClassHierarchyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CancelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.log("Pointer analysis done");
		Log.reportTime(":pointer_analysis_time");

		A a = new A(callGraph, pointerAnalysis, builder);

		foundCA = a.compute();
		Log.log("New Race analysis done");
		logSizeOfCallGraphAndMethods();

		return foundCA;
	}

	public void assertCAs(String string) {
		Assert.assertEquals("",string, this.foundCA.toString());
	}

	protected void logSizeOfCallGraphAndMethods() {
  	Log.report(":size_call_graph",callGraph.getNumberOfNodes());
  	Log.report(":size_methods", InstructionsGatherer.methods.size());
  }
}
