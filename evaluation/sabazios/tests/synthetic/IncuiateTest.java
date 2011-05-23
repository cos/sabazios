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

public class IncuiateTest extends DataRaceAnalysisTest {

	@Rule
	public TestName name = new TestName();

	public IncuiateTest() {
		super();
		DEBUG = true;
		this.addBinaryDependency("sabazios/synthetic");
		this.addBinaryDependency("../lib/parallelArray.mock");
		U.detailedResults = true;
	}

	@Before
	public void findCA() {
		foundCA = findCA("Lsabazios/synthetic/Incuiate", name.getMethodName() + "()V");
	}
	
	@Test
	public void simple() {
		assertCAs("");
	}
	
	@Test
	public void oneLevelLocalVar() {
		assertCAs("");
	}

	@Test
	public void syncedOnParallelArray() {
		assertCAs("");
	}
	
	@Test
	public void syncedOnSomeField() {
		assertCAs("");
	}
	
	@Test
	public void syncedOnStatic() {
		assertCAs("");
	}
	
	@Test
	public void syncedOnChangedStatic() {
		assertCAs("Loop: sabazios.synthetic.Incuiate.syncedOnChangedStatic(Incuiate.java:152)\n" + 
				"   Object : sabazios.synthetic.Incuiate.syncedOnChangedStatic(Incuiate.java:150) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$8.op(Incuiate.java:157) - .x { 7: sabazios.synthetic.Incuiate$8.op(Incuiate.java:156) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$8.op(Incuiate.java:157) - .x { 7: sabazios.synthetic.Incuiate$8.op(Incuiate.java:156) }\n" + 
				"   Object : null new Incuiate\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$8.op(Incuiate.java:155) - .lacatStatic\n" + 
				"      Other accesses:\n" + 
				"        Read sabazios.synthetic.Incuiate$8.op(Incuiate.java:156) - .lacatStatic\n" + 
				"        Write sabazios.synthetic.Incuiate$8.op(Incuiate.java:155) - .lacatStatic\n");
	}
	
	@Test
	public void syncedOnSomeChangedField() {
		assertCAs("Loop: sabazios.synthetic.Incuiate.syncedOnSomeChangedField(Incuiate.java:112)\n" + 
				"   Object : sabazios.synthetic.Incuiate.syncedOnSomeChangedField(Incuiate.java:110) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$6.op(Incuiate.java:115) - .origin\n" + 
				"      Other accesses:\n" + 
				"        Read sabazios.synthetic.Incuiate$6.op(Incuiate.java:116) - .origin\n" + 
				"        Write sabazios.synthetic.Incuiate$6.op(Incuiate.java:115) - .origin\n" + 
				"   Object : sabazios.synthetic.Incuiate.syncedOnSomeChangedField(Incuiate.java:110) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$6.op(Incuiate.java:117) - .x { 7: sabazios.synthetic.Incuiate$6.op(Incuiate.java:116) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$6.op(Incuiate.java:117) - .x { 7: sabazios.synthetic.Incuiate$6.op(Incuiate.java:116) }\n");
	}
	
	@Test
	public void stillARace() {
		assertCAs("Loop: sabazios.synthetic.Incuiate.stillARace(Incuiate.java:61)\n" + 
				"   Object : sabazios.synthetic.Incuiate.stillARace(Incuiate.java:59) new Particle\n" + 
				"      Write accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$3.op(Incuiate.java:65) - .x { 3: sabazios.synthetic.Incuiate$3.op(Incuiate.java:64) }\n" + 
				"      Other accesses:\n" + 
				"        Write sabazios.synthetic.Incuiate$3.op(Incuiate.java:65) - .x { 3: sabazios.synthetic.Incuiate$3.op(Incuiate.java:64) }\n");
	}
	
	@Test
	public void vectorDefaultSync() {
		assertCAs("");
	}
	
	@Test
	public void printStream() {
		assertCAs("");
	}
	
	@Ignore
	@Test
	public void templateMethod() {
		assertCAs("");
	}
}
