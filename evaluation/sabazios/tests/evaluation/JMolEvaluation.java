package sabazios.tests.evaluation;

import org.junit.Test;

import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.wala.WalaAnalysis;

public class JMolEvaluation extends DataRaceAnalysisTest {
  public JMolEvaluation() {
    super();
    this.addBinaryDependency("../evaluation/Jmol/bin");
    this.addBinaryDependency("../lib/parallelArray.mock");
    this.addJarFolderDependency("../evaluation/Jmol/lib");
  }

  @Test
  public void test() throws Exception {
    // findCA("LTemp", MAIN_METHOD);
    // findCA("Ltemp/org/openscience/app/Temp", MAIN_METHOD);

    // findCA("Lorg/openscience/jmol/app/Temp", MAIN_METHOD);
    // WalaAnalysis analysis = new WalaAnalysis();
    // findCA("Lorg/openscience/jmol/app/Jmol", MAIN_METHOD);
    // findCA("Lorg/jmol/shape/SticksRenderer", "render()V");

    setup("Lorg/openscience/jmol/app/Jmol", MAIN_METHOD);
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();

    @SuppressWarnings("deprecation")
    Graph<Object> prunedHP = GraphSlicer.prune(a.heapGraph, new Filter<Object>() {
      @Override
      public boolean accepts(Object o) {
        return noBanned(o) && isGraphicsObject(o);
      }

      private boolean isGraphicsObject(Object o) {
        String temp = o.toString();
        return temp.contains("SticksRenderer") || temp.contains("Circle3D") || temp.contains("Line3D")
            || temp.contains("Graphics3D") || temp.contains("Cylinder3D") || temp.contains("Triangle3D");//|| temp.contains("Atom");
      }

      private boolean noBanned(Object o) {
        String temp = o.toString();
        return !(temp.contains("Hermite3D") || temp.contains("String") || temp.contains("awt"));
      }

    });

    a.dotGraph(prunedHP, "Jmol" + "_JMol", null);

  }

}
