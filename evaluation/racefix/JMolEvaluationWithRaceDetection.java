package racefix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import sabazios.domains.ConcurrentFieldAccess;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;
import sabazios.util.wala.viz.HeapGraphNodeDecorator;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

public class JMolEvaluationWithRaceDetection extends DataRaceAnalysisTest {

  public JMolEvaluationWithRaceDetection() {
    super();
    this.addBinaryDependency("../evaluation/jmol/bin");
    this.addBinaryDependency("../lib/parallelArray.mock");
    this.addJarFolderDependency("../evaluation/jmol/lib");
    this.addBinaryDependency("racefix/jmol");
    this.addBinaryDependency("racefix/jmol/mock");
    U.detailedResults = false;
    this.projectName = "JMol";
  }

  @Test
  public void test() throws CancelException {
    String entryClass = "Lorg/openscience/jmol/app/Jmol";
    String entryMethod = MAIN_METHOD;
    runTest(entryClass, entryMethod);
  }

  @Test
  public void testMockVersion() throws Exception {
    String entryClass = "Lracefix/jmol/JmolEntryClass";
    String entryMethod = "testJmolEntryMethod()V";
    runTest(entryClass, entryMethod);
  }

  private void writeRacesToFile(Collection<Set<ConcurrentFieldAccess>> collection, String fileName) {
    try {
      System.out.println("--------\nPrinting to file\n-----------");
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      for (Set<ConcurrentFieldAccess> set : collection) {
        out.write("\n============================New SET===============================\n");
        for (ConcurrentFieldAccess fieldAccess : set) {
          out.write(fieldAccess.toString() + "\n");
        }
      }
      out.close();
      System.out.println("Done printing to file");
    } catch (Exception e) {// Catch exception if any
    }
  }

  private void runTest(String entryClass, String entryMethod) {
    findCA(entryClass, entryMethod);
    Set<ConcurrentFieldAccess> next = a.deepRaces.values().iterator().next();
    final Privatizer privatizer = new Privatizer(a, next);
    privatizer.compute();

    HeapGraph heapGraph = a.heapGraph;
    @SuppressWarnings("deprecation")
    Graph<Object> prunedGraph = GraphSlicer.prune(heapGraph, new Filter<Object>() {
      @Override
      public boolean accepts(Object o) {
        return privatizer.getInstancesToPrivatize().contains(o) || privatizer.getFieldNodesToPrivatize().contains(o);
      }
    });
    a.dotGraph(prunedGraph, name.getMethodName(), new HeapGraphNodeDecorator(heapGraph) {
      @Override
      public String getDecoration(Object obj) {
        if (privatizer.getFieldNodesToPrivatize().contains(obj))
          if (privatizer.shouldBeThreadLocal(((InstanceFieldKey) obj).getField()))
            return super.getDecoration(obj) + ", style=filled, fillcolor=red";
          else
            return super.getDecoration(obj) + ", style=filled, fillcolor=darkseagreen1";
        if (privatizer.getInstancesToPrivatize().contains(obj))
          return super.getDecoration(obj) + ", style=filled, fillcolor=darkseagreen1";
        return super.getDecoration(obj);
      }
    });
  }
}