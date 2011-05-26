package sabazios.tests;

import java.io.IOException;

import junit.framework.Assert;
import sabazios.A;
import sabazios.domains.ConcurrentAccesses;
import sabazios.util.Log;
import sabazios.wala.WalaAnalysis;

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

		A.init(callGraph, pointerAnalysis, builder);

		foundCA = A.compute();
		Log.log("New Race analysis done");

		return foundCA;
	}

	public void assertCAs(String string) {
		Assert.assertEquals("",string, this.foundCA.toString());
	}
}
