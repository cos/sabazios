package sabazios.tests.evaluation;

import org.junit.Test;

import sabazios.tests.DataRaceAnalysisTest;

public class VASSALEvaluation extends DataRaceAnalysisTest {
  
  public VASSALEvaluation() {
    addBinaryDependency("../vassal/bin");
    addBinaryDependency("../lib/parallelArray.mock");
    addJarFolderDependency("../vassal/lib");
    addJarFolderDependency("../vassal/lib-nondist");
  }
  
  @Test
  public void test() {
    findCA("LVASSAL/tools/image/GeneralFilter", "main([Ljava/lang/String;)V");
  }

}
