package racefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.collections.IndiscriminateFilter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

import racefix.AccessTrace;
import racefix.jmol.util.JmolCallGraphFilter;
import racefix.jmol.util.JmolHeapGraphFilter;
import racefix.util.AccessTraceFilter;
import sabazios.A;
import sabazios.CGNodeDecorator;
import sabazios.ColoredHeapGraphNodeDecorator;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;
import sabazios.wala.CS;

public class JMolEvaluation extends DataRaceAnalysisTest {
  public JMolEvaluation() {
    super();
    this.addBinaryDependency("../evaluation/Jmol/bin");
    this.addBinaryDependency("../lib/parallelArray.mock");
    this.addJarFolderDependency("../lib/Jmol");
  }

  @Test
  public void test() throws Exception {
    CS.NCFA = 0;
    Map<String, String> start = new HashMap<String, String>();
    start.put("plotLine\\(", "this");
    start.put("Cylinder3D, render\\(", "this");
    start.put("plotLineClipped\\(I", "zbuf");
    runJmol(start);
  }

  @SuppressWarnings("deprecation")
  private void runJmol(Map<String, String> traceStartingPoint) throws Exception {
    setup("Lorg/openscience/jmol/app/Jmol", MAIN_METHOD);
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();

    int i = 0;
    final AccessTrace[] traces = new AccessTrace[traceStartingPoint.size()];
    for (String methodName : traceStartingPoint.keySet()) {
      List<CGNode> possibleStartNode = a.findNodes(".*" + methodName + ".*");
      CGNode traceStartMethodeNode = possibleStartNode.get(0);

      String varName = traceStartingPoint.get(methodName);
      int ssaValue = 0;
      if (varName.equals("this"))
        ssaValue = 1;
      else
        ssaValue = U.getValueForVariableName(traceStartMethodeNode, traceStartingPoint.get(methodName));

      final AccessTrace accessTrace = new AccessTrace(a, traceStartMethodeNode, ssaValue,
          new IndiscriminateFilter<CGNode>());
      accessTrace.compute();
      traces[i++] = accessTrace;
    }
    Graph<Object> prunedHeapGraph = GraphSlicer.prune(a.heapGraph, new JmolHeapGraphFilter(traces));

    Graph<CGNode> prunedCallGraph = GraphSlicer.prune(a.callGraph, new JmolCallGraphFilter());

    ColoredHeapGraphNodeDecorator color = new ColoredHeapGraphNodeDecorator(prunedHeapGraph, new AccessTraceFilter(
        traces));
    a.dotGraph(prunedHeapGraph, "Jmol_heapGraph_color", color);
    a.dotGraph(prunedCallGraph, "Jmol_callGraph", new CGNodeDecorator(a));
  }

  @SuppressWarnings("unused")
  private static void printAccessTraces(AccessTrace[] traces) {
    for (AccessTrace trace : traces) {
      System.out.println("instanceKeys:");
      for (InstanceKey instKey : trace.getinstances())
        System.out.println(instKey.toString());
      System.out.println("pointerKeys:");
      for (PointerKey pointKey : trace.getPointers())
        System.out.println(pointKey);
      System.out.println("--------");
    }
  }

}
