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
				"        Write sabazios.synthetic.Lacate$2.op(Lacate.java:33) - .x { sabazios.synthetic.Lacate$2.op(Lacate.java:32) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$2.op(Lacate.java:33) - .x { sabazios.synthetic.Lacate$2.op(Lacate.java:32) }\n");
	}
	
	@Test
	public void lockUsingSynchronizedBlockInAnotherMethod() {
		assertCAs("Loop: sabazios.synthetic.Lacate.lockUsingSynchronizedBlockInAnotherMethod(Lacate.java:46)\n" + 
				"   Object : sabazios.synthetic.Lacate.lockUsingSynchronizedBlockInAnotherMethod(Lacate.java:44) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$3.someMethod(Lacate.java:56) - .x { sabazios.synthetic.Lacate$3.op(Lacate.java:49) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Lacate$3.someMethod(Lacate.java:56) - .x { sabazios.synthetic.Lacate$3.op(Lacate.java:49) }\n");
	}
}
