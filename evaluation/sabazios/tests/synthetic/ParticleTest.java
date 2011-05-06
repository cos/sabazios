package sabazios.tests.synthetic;

import org.junit.Ignore;
import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

public class ParticleTest extends DataRaceAnalysisTest {

	public ParticleTest() {
		super();
		DEBUG = true;
		this.addBinaryDependency("synthetic");
		this.addBinaryDependency("../lib/parallelArray.mock");
		U.detailedResults = false;
	}
	
	@Test 
	public void vacuouslyNoRace() throws CancelException {
		findCA();
		assertNoCA();
	}

	@Test 
	public void noRaceOnParameter() throws CancelException {
		findCA();
		assertNoCA();
	}

	@Test 
	public void verySimpleRace() {
		findCA();
		assertCAs(
				"Loop: synthetic.Particle.verySimpleRace(Particle.java:64)\n" + 
				"   Object : synthetic.Particle.verySimpleRace(Particle.java:62) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write synthetic.Particle$5.op(Particle.java:67) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write synthetic.Particle$5.op(Particle.java:67) - .x\n");
	}

	@Test 
	public void verySimpleRaceWithIndex() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:379)\n" + 
				"   Object : (synthetic/Particle.java:377) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:382) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:382) - .x\n");
	}
	
	/**
	 * Is there a problem when the elements are initialized in another forall?
	 */
	@Test 
	public void noRaceOnParameterInitializedBefore() throws CancelException  {
		findCA();
		assertNoCA();
	}
	
	/**
	 * an part of an element is tainted in another forall
	 */
	@Test 
	public void raceOnParameterInitializedBefore() throws CancelException  {
		findCA();
		assertCAs("Loop: synthetic.Particle.raceOnParameterInitializedBefore(Particle.java:88)\n" + 
				"   Object : synthetic.Particle.raceOnParameterInitializedBefore(Particle.java:77) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write synthetic.Particle$7.op(Particle.java:91) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write synthetic.Particle$7.op(Particle.java:91) - .x\n");
	}
	
	/**
	 * Is it field sensitive?
	 */
	@Test 
	public void noRaceOnANonSharedField() throws CancelException {
		findCA();
		assertNoCA();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on 0-CFA
	 * Works on 1-CFA
	 */
	@Test 
	public void oneCFANeeded() throws CancelException {
		findCA();
		assertNoCA();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on 0-CFA and 1-CFA
	 * Works on 2-CFA
	 */
	@Test 
	public void twoCFANeeded() throws CancelException {
		findCA();
		assertNoCA();
	}
	
	/**
	 * How context sensitive is it?
	 * Fails on any CFA due to recursivity
	 * Might work on smarter analyses
	 */
	@Test 
	public void recursive() throws CancelException {
		findCA();
		assertNoCA();
	}
	
	/**
	 * Disambiguate the trace for a race.
	 * The trace should contain "shared.moveTo(5, 7);" but not
	 * "particle.moveTo(2, 3);"
	 */
	@Test 
	public void disambiguateFalseRace() throws CancelException {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:185)\n" + 
				"   Object : (synthetic/Particle.java:182) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:12) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:12) - .x\n" + 
				"   Object : (synthetic/Particle.java:182) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:13) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:13) - .y\n");
	}
	
	@Test 
	public void ignoreFalseRacesInSeqOp() {
		findCA();
		assertNoCA();
	}
	
	@Test 
	public void raceBecauseOfOutsideInterference()  {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:228)\n" + 
				"   Object : (synthetic/Particle.java:231) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:232) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:232) - .x\n" + 
				"   Object : (synthetic/Particle.java:225) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:231) - .origin\n" + 
				"      Other accesses:\n" + 
				"        Read (synthetic/Particle.java:232) - .origin\n" + 
				"        Write (synthetic/Particle.java:231) - .origin\n");
	}
	
	@Test 
	public void raceOnSharedObjectCarriedByArray() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:255)\n" + 
				"   Object : (synthetic/Particle.java:249) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:12) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:12) - .x\n" + 
				"   Object : (synthetic/Particle.java:249) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:13) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:13) - .y\n");
	}
	
	@Test 
	public void raceBecauseOfDirectArrayLoad() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:270)\n" + 
				"   Object : (synthetic/Particle.java:275) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:274) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:274) - .x\n" + 
				"   Object : (synthetic/Particle.java:267) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:274) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:274) - .x\n");
	}
	
	@Test 
	public void verySimpleRaceToStatic() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:394)\n" + 
				"   Object : (synthetic/Particle.java:388) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:397) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:397) - .x\n");
	}
	
	@Override
	protected String getTestedClassName() {
		return "Lsynthetic/Particle";
	};
	
	
	@Test 
	public void raceOnSharedReturnValue() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:286)\n" + 
				"   Object : (synthetic/Particle.java:284) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:289) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:289) - .x\n");
	}
	
	@Test
	public void raceOnDifferntArrayIteration() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:313)\n" + 
				"   Object : (synthetic/Particle.java:302) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:316) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:316) - .x\n");
	}
	
	@Test @Ignore
	public void noRaceIfFlowSensitive() {
		findCA();
		assertNoCA();
	}
	
	@Test
	public void raceOnDifferntArrayIterationOneLoop() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:363)\n" + 
				"   Object : (synthetic/Particle.java:367) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:366) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:366) - .x\n" + 
				"   Object : (synthetic/Particle.java:361) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:367) - .origin\n" + 
				"      Other accesses:\n" + 
				"        Read (synthetic/Particle.java:368) - .origin\n" + 
				"        Write (synthetic/Particle.java:367) - .origin\n");
	}
	
	@Test
	public void raceOnSharedFromStatic() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:407)\n" + 
				"   Object : (synthetic/Particle.java:388) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:411) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:411) - .y\n");
	}
	
	@Test
	public void raceInLibrary() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:423)\n" + 
				"   Object : (synthetic/Particle.java:421) new HashSet\n" + 
				"      Write accesses:\n" + 
				"        (synthetic/Particle.java:427)\n" + 
				"      Other accesses:\n" + 
				"        (synthetic/Particle.java:427)\n" + 
				"        (synthetic/Particle.java:428)\n");
	}
	
	@Test
	public void noRaceOnStringConcatenation() {
		findCA();
		assertNoCA();
	}

	@Test
	public void noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:454)\n" + 
				"   Object : (synthetic/Particle.java:452) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:458) - .origin\n" + 
				"      Other accesses:\n" + 
				"        Write (synthetic/Particle.java:458) - .origin\n");
	}
	
	@Test
	public void noRaceWhenPrintln() {
		findCA();
		assertNoCA();
	}
	
	@Test
	public void raceOnArray() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:484)\n" + 
				"   Object : (synthetic/Particle.java:482) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:487) - .[*]\n" + 
				"      Other accesses:\n" + 
				"        Read (synthetic/Particle.java:487) - .[*]\n");
	}
	
	@Test 
	public void raceByWriteOnSomethingInstantiatedInTheMainIteration() {
		findCA();
		assertCAs("Loop: (synthetic/Particle.java:508)\n" + 
				"   Object : (synthetic/Particle.java:500) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write (synthetic/Particle.java:511) - .x\n" + 
				"      Other accesses:\n" + 
				"        Read (synthetic/Particle.java:512) - .x\n");
	}
	
	@Test
	public void multipleArrays() {
		findCA();
		assertCAs("Loop: synthetic.Particle.multipleArrays(Particle.java:539)\n" + 
				"   Object : synthetic.Particle$38.op(Particle.java:526) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write synthetic.Particle$40.op(Particle.java:543) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write synthetic.Particle$40.op(Particle.java:543) - .y\n");
	}
	
	@Test
	public void multipleArrays1CFANeeded() {
		findCA();
		assertCAs("Loop: synthetic.Particle.multipleArrays1CFANeeded(Particle.java:568)\n" + 
				"   Object : synthetic.Particle$41.op(Particle.java:555) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write synthetic.Particle$43.op(Particle.java:572) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write synthetic.Particle$43.op(Particle.java:572) - .y\n");
	}
}
