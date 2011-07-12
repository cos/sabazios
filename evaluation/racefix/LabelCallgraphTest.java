package racefix;

import org.junit.Test;
import sabazios.tests.DataRaceAnalysisTest;

public class LabelCallgraphTest extends DataRaceAnalysisTest {

	public LabelCallgraphTest() {
		super();
		this.addBinaryDependency("racefix");
		this.addBinaryDependency("../lib/parallelArray.mock");
	}

	@Test
	public void test() throws Exception {
		setup("Lracefix/Foo", "simple()V");
	}
}