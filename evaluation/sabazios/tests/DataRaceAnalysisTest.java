package sabazios.tests;

import java.io.IOException;

import junit.framework.Assert;
import sabazios.ConcurrentAccesses;
import sabazios.RaceAnalysis;
import sabazios.WalaAnalysis;
import sabazios.util.Log;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

public abstract class DataRaceAnalysisTest extends WalaAnalysis {

	// what files/classes to include?
	// what are the entrypoints?

	protected String testClassName;
	protected static boolean DEBUG = false;
	private ConcurrentAccesses foundCA;

	public DataRaceAnalysisTest() {
		Log.start();
		testClassName = this.getClass().getName();
	}

	public ConcurrentAccesses findCA(String entryClass, String entryMethod) {

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

		RaceAnalysis analysis = new RaceAnalysis(callGraph, pointerAnalysis, builder);

		analysis.compute();
		Log.log("New Race analysis done");

		return analysis.getRaces();
	}

	public void findCA() {
		foundCA = findCA(getTestedClassName(), getEntryMethod());
	}

	protected String getEntryMethod() {
		return getCurrentlyExecutingTestName() + "()V";
	}

	protected String getTestedClassName() {
		String testClassName = this.getClass().getSimpleName();
		String className = testClassName.substring(0, testClassName.length() - 4);
		String classNameForWala = "Lsynthetic/" + className;
		return classNameForWala;
	}

	protected String getCurrentlyExecutingTestName() {
		Throwable t = new Throwable();
		StackTraceElement[] elements = t.getStackTrace();
		if (elements.length <= 0)
			return "[No Stack Information Available]";
		// elements[0] is this method
		if (elements.length < 2)
			return null;

		for (StackTraceElement stackTraceElement : elements) {
			if (stackTraceElement.getClassName().equals(testClassName))
				return stackTraceElement.getMethodName();
		}
		return "[Not in a test hierarchy]";
	}

	public void assertNoCA() {
		Assert.assertTrue("These races shoulnd't be here: " + this.foundCA, this.foundCA.accesses.isEmpty());
	}

	public void assertCAs(String string) {
		Assert.assertEquals(string, this.foundCA.toString());
	}
}
