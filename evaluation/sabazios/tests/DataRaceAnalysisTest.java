package sabazios.tests;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.ComparisonFailure;

import sabazios.ConcurrentAccesses;
import sabazios.ConcurrentFieldAccesses;
import sabazios.RaceAnalysis;
import sabazios.util.U;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.Analysis;
import edu.illinois.reLooper.sabazios.CodeLocation;
import edu.illinois.reLooper.sabazios.WalaAnalysis;
import edu.illinois.reLooper.sabazios.log.Log;
import edu.illinois.reLooper.sabazios.raceObjects.Race;
import edu.illinois.reLooper.sabazios.raceObjects.RaceOnNonStatic;

public abstract class DataRaceAnalysisTest extends WalaAnalysis {

	// what files/classes to include?
	// what are the entrypoints?

	protected String testClassName;
	protected static boolean DEBUG = false;
	protected Analysis analysis;
	protected Set<Race> foundRaces;
	private ConcurrentAccesses foundCA;

	public DataRaceAnalysisTest() {
		Log.start();
		testClassName = this.getClass().getName();
	}

	public Set<Race> findRaces(String entryClass, String entryMethod) {
//		if(true)
//			return null;
//
//		try {
//			if (pointerAnalysis == null)
//				setup(entryClass, entryMethod);
//		} catch (ClassHierarchyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CancelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		Log.log("Pointer analysis done");
//		analysis = new Analysis(callGraph, pointerAnalysis, builder);
//
//		// return analysis.getRaces();
//
//		RaceFinder rf = new RaceFinder(analysis);
//		Set<Race> races = null;
//		try {
//			races = rf.findRaces();
//		} catch (CancelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		Log.log("Data race analysis done");
//		RaceSetTransformer transformRaces = new RaceSetTransformer(analysis);
//		Set<Race> shallowRaces = transformRaces.transform(races);
//		return shallowRaces;
		return new HashSet<Race>();
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

	protected void printDetailedRaces(Set<Race> foundRaces) {
		System.out.println("Number of races: " + foundRaces.size());
		for (Iterator<Race> iterator = foundRaces.iterator(); iterator.hasNext();) {
			Race race = iterator.next();
			System.out.println(race.toDetailedString(analysis));
		}
	}

	protected void printRaces(Set<RaceOnNonStatic> races) {
		System.out.println("Number of races: " + races.size());
		for (Iterator<RaceOnNonStatic> iterator = races.iterator(); iterator.hasNext();) {
			RaceOnNonStatic race = (RaceOnNonStatic) iterator.next();
			System.out.println(race);
		}
	}

	public void assertNoRaces() {
		assertRaces();
	}

	public void findRaces() {
		foundRaces = findRaces(getTestedClassName(), getEntryMethod());
	}

	public void findCA() {
		foundCA = findCA(getTestedClassName(), getEntryMethod());
	}

	public void assertRaces(String... expected) {
		List<String> expectedRaces = Arrays.asList(expected);
		if (DEBUG) {
			System.err.println(getCurrentlyExecutingTestName());
			printDetailedRaces(foundRaces);
		}
		if (foundRaces.size() == 0 && expectedRaces == null)
			return;
		if (expectedRaces != null && expectedRaces.size() == foundRaces.size()) {
			boolean noMatch = false;
			for (Race r : foundRaces) {
				if (!expectedRaces.contains(CodeLocation.make(r.getStatement()).toString())) {
					System.out.println(CodeLocation.make(r.getStatement()).toString());
					noMatch = true;
					break;
				}
			}
			if (noMatch)
				fail(expectedRaces, foundRaces);
			else
				return;
		}

		fail(expectedRaces, foundRaces);
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

	private void fail(Collection<String> expectedRaces, Set<Race> foundRaces) throws ComparisonFailure {
		String expected = "";
		if (expectedRaces != null)
			for (String s : expectedRaces)
				expected += s + "\n";

		System.err.println("Expected: \n" + expected + "Found:");
		String actual = "";
		for (Race r : foundRaces) {
			actual += CodeLocation.make(r.getStatement()) + "\n";
			System.err.println(r);
		}

		throw new ComparisonFailure("Different data races", expected, actual);
	}

	protected void assertRace(String location) {
		assertRaces(location);
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
	
	public void detailedPrintRaces() {
		if(true)
			return;
		System.out.println("Found " + foundRaces.size() + " races");
		int i = 0;
		Iterator<Race> iterator = foundRaces.iterator();
		while (i < foundRaces.size()) {
			i++;
			Race r = iterator.next();
			System.out.println(r.toDetailedString(analysis));
		}
	}
}
