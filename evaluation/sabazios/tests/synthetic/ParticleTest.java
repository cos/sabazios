package sabazios.tests.synthetic;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.tests.RaceAssert;
import sabazios.util.U;

import com.ibm.wala.util.CancelException;

public class ParticleTest extends DataRaceAnalysisTest {

	@Rule
	public TestName name = new TestName();

	public ParticleTest() {
		super();
		DEBUG = true;
		this.addBinaryDependency("sabazios/synthetic");
		this.addBinaryDependency("../lib/parallelArray.mock");
		U.detailedResults = false;
	}

	@Before
	public void findCA() {
		foundCA = findCA("Lsabazios/synthetic/Particle", name.getMethodName() + "()V");
	}

	@Test
	public void vacuouslyNoRace() throws CancelException {
		assertCAs("");
	}

	@Test
	public void noRaceOnParameter() throws CancelException {
		assertCAs("");
	}

	@Test
	public void verySimpleRace() {
		assertCAs("Loop: sabazios.synthetic.Particle.verySimpleRace(Particle.java:67)\n" + 
				"   Object : sabazios.synthetic.Particle.verySimpleRace(Particle.java:65) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$5.op(Particle.java:70) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$5.op(Particle.java:70) - .x\n");
	}

	private RaceAssert assertRace() {
		return new RaceAssert(foundCA);
	}

	@Test
	public void verySimpleRaceWithIndex() {
		assertCAs("Loop: sabazios.synthetic.Particle.verySimpleRaceWithIndex(Particle.java:382)\n" + 
				"   Object : sabazios.synthetic.Particle.verySimpleRaceWithIndex(Particle.java:380) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$28.op(Particle.java:385) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$28.op(Particle.java:385) - .x\n");
	}

	/**
	 * Is there a problem when the elements are initialized in another forall?
	 */
	@Test
	public void noRaceOnParameterInitializedBefore() throws CancelException {
		assertCAs("");
	}

	/**
	 * an part of an element is tainted in another forall
	 */
	@Test
	public void raceOnParameterInitializedBefore() throws CancelException {
		assertCAs("Loop: sabazios.synthetic.Particle.raceOnParameterInitializedBefore(Particle.java:91)\n" + 
				"   Object : sabazios.synthetic.Particle.raceOnParameterInitializedBefore(Particle.java:80) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$7.op(Particle.java:94) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$7.op(Particle.java:94) - .x\n");
	}

	/**
	 * Is it field sensitive?
	 */
	@Test
	public void noRaceOnANonSharedField() throws CancelException {
		assertCAs("");
	}

	/**
	 * How context sensitive is it? Fails on 0-CFA Works on 1-CFA
	 */
	@Test
	public void oneCFANeeded() throws CancelException {
		assertCAs("");
	}

	/**
	 * How context sensitive is it? Fails on 0-CFA and 1-CFA Works on 2-CFA
	 */
	@Test
	public void twoCFANeeded() throws CancelException {
		assertCAs("");
	}

	/**
	 * How context sensitive is it? Fails on any CFA due to recursivity Might
	 * work on smarter analyses
	 */
	@Test
	public void recursive() throws CancelException {
		assertCAs("");
	}

	/**
	 * Disambiguate the trace for a race. The trace should contain
	 * "shared.moveTo(5, 7);" but not "particle.moveTo(2, 3);"
	 */
	@Test
	public void disambiguateFalseRace() throws CancelException {
		assertCAs("Loop: sabazios.synthetic.Particle.disambiguateFalseRace(Particle.java:188)\n" + 
				"   Object : sabazios.synthetic.Particle.disambiguateFalseRace(Particle.java:185) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:15) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:15) - .x\n" + 
				"   Object : sabazios.synthetic.Particle.disambiguateFalseRace(Particle.java:185) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:16) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:16) - .y\n");
	}

	@Test
	public void ignoreFalseRacesInSeqOp() {
		assertCAs("");
	}

	@Test
	public void raceBecauseOfOutsideInterference() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceBecauseOfOutsideInterference(Particle.java:231)\n" + 
				"   Object : sabazios.synthetic.Particle$15.op(Particle.java:234) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$15.op(Particle.java:235) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$15.op(Particle.java:235) - .x\n" + 
				"   Object : sabazios.synthetic.Particle.raceBecauseOfOutsideInterference(Particle.java:228) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$15.op(Particle.java:234) - .origin\n" + 
				"      Other accesses:\n" + 
				"        Read sabazios.synthetic.Particle$15.op(Particle.java:235) - .origin\n" + 
				"        Write sabazios.synthetic.Particle$15.op(Particle.java:234) - .origin\n");
	}

	@Test
	public void raceOnSharedObjectCarriedByArray() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceOnSharedObjectCarriedByArray(Particle.java:258)\n" + 
				"   Object : sabazios.synthetic.Particle$16.op(Particle.java:252) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:15) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:15) - .x\n" + 
				"   Object : sabazios.synthetic.Particle$16.op(Particle.java:252) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:16) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle.moveTo(Particle.java:16) - .y\n");
	}

	@Test
	public void raceBecauseOfDirectArrayLoad() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceBecauseOfDirectArrayLoad(Particle.java:273)\n" + 
				"   Object : sabazios.synthetic.Particle$18.op(Particle.java:278) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$18.op(Particle.java:277) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$18.op(Particle.java:277) - .x\n" + 
				"   Object : sabazios.synthetic.Particle.raceBecauseOfDirectArrayLoad(Particle.java:270) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$18.op(Particle.java:277) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$18.op(Particle.java:277) - .x\n");
	}

	@Test
	public void verySimpleRaceToStatic() {
		assertCAs("Loop: sabazios.synthetic.Particle.verySimpleRaceToStatic(Particle.java:398)\n" + 
				"   Object : sabazios.synthetic.Particle.<clinit>(Particle.java:391) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$29.op(Particle.java:401) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$29.op(Particle.java:401) - .x\n");
	}

	@Test
	public void raceOnSharedReturnValue() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceOnSharedReturnValue(Particle.java:289)\n" + 
				"   Object : sabazios.synthetic.Particle.raceOnSharedReturnValue(Particle.java:287) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$19.op(Particle.java:292) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$19.op(Particle.java:292) - .x\n");
	}

	@Test
	public void raceOnDifferntArrayIteration() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceOnDifferntArrayIteration(Particle.java:316)\n" + 
				"   Object : sabazios.synthetic.Particle$20.op(Particle.java:305) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$22.op(Particle.java:319) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$22.op(Particle.java:319) - .x\n");
	}

	@Test
	@Ignore
	public void noRaceIfFlowSensitive() {
		assertCAs("");
	}

	@Test
	public void raceOnDifferntArrayIterationOneLoop() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:366)\n" + 
				"   Object : sabazios.synthetic.Particle$27.op(Particle.java:370) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$27.op(Particle.java:369) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$27.op(Particle.java:369) - .x\n" + 
				"   Object : sabazios.synthetic.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:364) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$27.op(Particle.java:370) - .origin\n" + 
				"      Other accesses:\n" + 
				"        Read sabazios.synthetic.Particle$27.op(Particle.java:371) - .origin\n" + 
				"        Write sabazios.synthetic.Particle$27.op(Particle.java:370) - .origin\n");
	}

	@Test
	public void raceOnSharedFromStatic() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceOnSharedFromStatic(Particle.java:411)\n" + 
				"   Object : sabazios.synthetic.Particle.<clinit>(Particle.java:391) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$30.op(Particle.java:415) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$30.op(Particle.java:415) - .y\n");
	}

	@Test
	public void raceInLibrary() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceInLibrary(Particle.java:427)\n" + 
				"   Object : sabazios.synthetic.Particle.raceInLibrary(Particle.java:425) new HashSet\n" + 
				"      Write accesses:\n" + 
				"        sabazios.synthetic.Particle$31.op(Particle.java:431)\n" + 
				"      Other accesses:\n" + 
				"        sabazios.synthetic.Particle$31.op(Particle.java:431)\n" + 
				"        sabazios.synthetic.Particle$31.op(Particle.java:432)\n");
	}

	@Test
	public void noRaceOnStringConcatenation() {
		assertCAs("");
	}

	@Test
	public void noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape() {
		assertCAs("Loop: sabazios.synthetic.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:459)\n" + 
				"   Object : sabazios.synthetic.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:457) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$33.op(Particle.java:463) - .origin\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$33.op(Particle.java:463) - .origin\n");
	}

	@Test
	public void noRaceWhenPrintln() {
		assertCAs("");
	}

	@Test
	public void raceOnArray() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceOnArray(Particle.java:489)\n" + 
				"   Object : sabazios.synthetic.Particle.raceOnArray(Particle.java:487) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$35.op(Particle.java:492) - .[*]\n" + 
				"      Other accesses:\n" + 
				"        Read sabazios.synthetic.Particle$35.op(Particle.java:492) - .[*]\n");
	}

	@Test
	public void raceByWriteOnSomethingInstantiatedInTheMainIteration() {
		assertCAs("Loop: sabazios.synthetic.Particle.raceByWriteOnSomethingInstantiatedInTheMainIteration(Particle.java:511)\n" + 
				"   Object : sabazios.synthetic.Particle$36.op(Particle.java:505) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$37.op(Particle.java:514) - .x\n" + 
				"      Other accesses:\n" + 
				"        Read sabazios.synthetic.Particle$37.op(Particle.java:515) - .x\n");
	}

	@Test
	public void multipleArrays() {
		assertCAs("Loop: sabazios.synthetic.Particle.multipleArrays(Particle.java:543)\n" + 
				"   Object : sabazios.synthetic.Particle$38.op(Particle.java:530) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$40.op(Particle.java:547) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$40.op(Particle.java:547) - .y\n");
	}

	@Test
	public void multipleArrays1CFANeeded() {
		assertCAs("Loop: sabazios.synthetic.Particle.multipleArrays1CFANeeded(Particle.java:572)\n" + 
				"   Object : sabazios.synthetic.Particle$41.op(Particle.java:559) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Particle$43.op(Particle.java:576) - .y\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Particle$43.op(Particle.java:576) - .y\n");
	}
}
