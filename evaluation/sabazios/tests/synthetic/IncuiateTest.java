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
		U.detailedResults = false;
	}

	@Before
	public void findCA() {
		foundCA = findCA("Lsabazios/synthetic/Incuiate", name.getMethodName() + "()V");
	}
	
	@Test
	public void simple() {
		assertCAs("");
	}
	
	@Ignore
	@Test
	public void templateMethod() {
		assertCAs("");
	}
}
