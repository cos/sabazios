package racefix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import racefix.util.ConcurrentAccessFilter;
import racefix.util.DummyFilter;
import racefix.util.PrintUtil;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;
import sabazios.util.wala.viz.HeapGraphNodeDecorator;
import sabazios.wala.CS;

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
  public void testWithEvaluation() throws CancelException {
    CS.NCFA = 0;
    String entryClass = "Lorg/openscience/jmol/app/Jmol";
    String entryMethod = MAIN_METHOD;
    ConcurrentAccessFilter filter = new DummyFilter();
    Privatizer runTest = runTest(entryClass, entryMethod, filter, true);
  }

  @Test
  public void testWithEvaluationMockVersion() throws Exception {
    CS.NCFA = 1;
    String entryClass = "Lracefix/jmol/JmolEntryClass";
    String entryMethod = "testJmolEntryMethod()V";
    ConcurrentAccessFilter filter = new ConcurrentAccessFilter() {
      @Override
      public Set<ConcurrentFieldAccess> filter(Set<ConcurrentFieldAccess> setOfAccesses) {
        Set<ConcurrentFieldAccess> filteredAccesses = new LinkedHashSet<ConcurrentFieldAccess>();

        String[] strings = { "width"};

        for (ConcurrentFieldAccess concurrentFieldAccess : setOfAccesses) {
          for (String s : strings)
            if (concurrentFieldAccess.toString().contains(s))
              filteredAccesses.add(concurrentFieldAccess);
        }
        return filteredAccesses;
      }
    };
    
    Privatizer runTest = runTest(entryClass, entryMethod, new DummyFilter(), true);
  }

  private Privatizer runTest(String entryClass, String entryMethod, ConcurrentAccessFilter filter, boolean writeStuff) {
    findCA(entryClass, entryMethod);
    Set<ConcurrentFieldAccess> races = a.deepRaces.values().iterator().next();

    races = filter.filter(races);

    final Privatizer privatizer = new Privatizer(a, races);
    privatizer.compute();

    if (writeStuff) {
      PrintUtil.writeLCDs(privatizer.getAccessesInLCDTestString(), name.getMethodName() + "LCDs.txt");
      PrintUtil.writeRacesToFile(a.deepRaces.values(), name.getMethodName() + "Races.txt");
    }

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

    return privatizer;
  }

}