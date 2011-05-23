package sabazios.tests;

import java.io.IOException;

import junit.framework.Assert;
import sabazios.ConcurrentAccesses;
import sabazios.A;
import sabazios.WalaAnalysis;
import sabazios.util.Log;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

public abstract class DataRaceAnalysisTest extends WalaAnalysis {

	// what files/classes to include?
	// what are the entrypoints?

	protected String testClassName;
	protected static boolean DEBUG = false;
	protected ConcurrentAccesses foundCA;

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

		A analysis = new A(callGraph, pointerAnalysis, builder);

		analysis.compute();
		Log.log("New Race analysis done");

		return analysis.getRaces();
	}

	public void assertCAs(String string) {
		Assert.assertEquals("",string, this.foundCA.toString());
	}
}
