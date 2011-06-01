package sabazios.tests.synthetic;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;

public class LockTest extends DataRaceAnalysisTest {

	@Rule
	public TestName name = new TestName();

	public LockTest() {
		super();
		DEBUG = true;
		this.addBinaryDependency("sabazios/synthetic");
		this.addBinaryDependency("../lib/parallelArray.mock");
		U.detailedResults = false;
	}

	@Before
	public void findCA() {
		foundCA = findCA("Lsabazios/synthetic/Lacate", name.getMethodName() + "()V");
	}

	@Test
	public void noLocks() {
		assertCAs("Loop: sabazios.synthetic.Lacate.noLocks(Lacate.java:25)\n" + 
				"   Object : sabazios.synthetic.Lacate.noLocks(Lacate.java:23) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$1.op(Lacate.java:28) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$1.op(Lacate.java:28) - .x\n");
	}
	
	@Test
	public void lockUsingSynchronizedBlock() {
		assertCAs("Loop: sabazios.synthetic.Lacate.lockUsingSynchronizedBlock(Lacate.java:40)\n" + 
				"   Object : sabazios.synthetic.Lacate.lockUsingSynchronizedBlock(Lacate.java:38) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$2.op(Lacate.java:44) - .x { 1: sabazios.synthetic.Lacate$2.op(Lacate.java:43) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$2.op(Lacate.java:44) - .x { 1: sabazios.synthetic.Lacate$2.op(Lacate.java:43) }\n");
	}
	
	@Test
	public void lockUsingSynchronizedBlockInAnotherMethod() {
		assertCAs("Loop: sabazios.synthetic.Lacate.lockUsingSynchronizedBlockInAnotherMethod(Lacate.java:60)\n" + 
				"   Object : sabazios.synthetic.Lacate.lockUsingSynchronizedBlockInAnotherMethod(Lacate.java:58) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$3.someMethod(Lacate.java:70) - .x { 1: sabazios.synthetic.Lacate$3.op(Lacate.java:63) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$3.someMethod(Lacate.java:70) - .x { 1: sabazios.synthetic.Lacate$3.op(Lacate.java:63) }\n");
	}
	
	@Test
	public void lockFromBothSynchronizedAndUnsynchronized() {
		assertCAs("Loop: sabazios.synthetic.Lacate.lockFromBothSynchronizedAndUnsynchronized(Lacate.java:85)\n" + 
				"   Object : sabazios.synthetic.Lacate.lockFromBothSynchronizedAndUnsynchronized(Lacate.java:83) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$4.someMethod(Lacate.java:96) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$4.someMethod(Lacate.java:96) - .x\n");
	}
	
	@Test
	public void synchronizedMethod() {
		assertCAs("Loop: sabazios.synthetic.Lacate.synchronizedMethod(Lacate.java:107)\n" + 
				"   Object : sabazios.synthetic.Lacate.synchronizedMethod(Lacate.java:105) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$5.op(Lacate.java:110) - .x { 1: null , 1: sabazios.synthetic.Lacate$5.op(Lacate.java:109) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$5.op(Lacate.java:110) - .x { 1: null , 1: sabazios.synthetic.Lacate$5.op(Lacate.java:109) }\n");
	}
	
	@Test
	public void synchronizedStaticMethod() {
		assertCAs("Loop: sabazios.synthetic.Lacate.synchronizedStaticMethod(Lacate.java:122)\n" + 
				"   Object : sabazios.synthetic.Lacate.synchronizedStaticMethod(Lacate.java:120) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate.raceBabyRace(Lacate.java:132) - .x { S : Lsabazios/synthetic/Lacate }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate.raceBabyRace(Lacate.java:132) - .x { S : Lsabazios/synthetic/Lacate }\n");
	}
	
	@Ignore
	@Test
	public void reenterantLock() {
		assertCAs("");
	}
	
	
	public void templateMethod() {
		assertCAs("");
	}
}
