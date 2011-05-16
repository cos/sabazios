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
		assertCAs("Loop: sabazios.synthetic.Lacate.noLocks(Lacate.java:14)\n" + 
				"   Object : sabazios.synthetic.Lacate.noLocks(Lacate.java:12) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$1.op(Lacate.java:17) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$1.op(Lacate.java:17) - .x\n");
	}
	
	@Test
	public void lockUsingSynchronizedBlock() {
		assertCAs("Loop: sabazios.synthetic.Lacate.lockUsingSynchronizedBlock(Lacate.java:29)\n" + 
				"   Object : sabazios.synthetic.Lacate.lockUsingSynchronizedBlock(Lacate.java:27) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$2.op(Lacate.java:33) - .x { 1: sabazios.synthetic.Lacate$2.op(Lacate.java:31) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$2.op(Lacate.java:33) - .x { 1: sabazios.synthetic.Lacate$2.op(Lacate.java:31) }\n");
	}
	
	@Test
	public void lockUsingSynchronizedBlockInAnotherMethod() {
		assertCAs("Loop: sabazios.synthetic.Lacate.lockUsingSynchronizedBlockInAnotherMethod(Lacate.java:49)\n" + 
				"   Object : sabazios.synthetic.Lacate.lockUsingSynchronizedBlockInAnotherMethod(Lacate.java:47) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$3.someMethod(Lacate.java:59) - .x { sabazios.synthetic.Lacate$3.op(Lacate.java:52) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$3.someMethod(Lacate.java:59) - .x { sabazios.synthetic.Lacate$3.op(Lacate.java:52) }\n");
	}
	
	@Test
	public void lockFromBothSynchronizedAndUnsynchronized() {
		assertCAs("Loop: sabazios.synthetic.Lacate.lockFromBothSynchronizedAndUnsynchronized(Lacate.java:73)\n" + 
				"   Object : sabazios.synthetic.Lacate.lockFromBothSynchronizedAndUnsynchronized(Lacate.java:71) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$4.someMethod(Lacate.java:84) - .x\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$4.someMethod(Lacate.java:84) - .x\n");
	}
	
	@Test
	public void synchronizedMethod() {
		assertCAs("Loop: sabazios.synthetic.Lacate.synchronizedMethod(Lacate.java:96)\n" + 
				"   Object : sabazios.synthetic.Lacate.synchronizedMethod(Lacate.java:94) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$5.op(Lacate.java:99) - .x { 1: sabazios.synthetic.Lacate$5.op(Lacate.java:98) , 1: null }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$5.op(Lacate.java:99) - .x { 1: null , 1: sabazios.synthetic.Lacate$5.op(Lacate.java:98) }\n");
	}
}
