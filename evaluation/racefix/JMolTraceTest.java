package racefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.rules.TestName;

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
import racefix.util.PrintUtil;
import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;
import sabazios.util.wala.viz.CGNodeDecorator;
import sabazios.util.wala.viz.ColoredHeapGraphNodeDecorator;
import sabazios.wala.CS;

@SuppressWarnings("deprecation")
public class JMolTraceTest extends DataRaceAnalysisTest {
  public JMolTraceTest() {
    super();
    this.addBinaryDependency("../evaluation/jmol/bin");
    this.addBinaryDependency("../lib/parallelArray.mock");
    this.addBinaryDependency("racefix/jmol");
    this.addBinaryDependency("racefix/jmol/mock");
    this.addJarFolderDependency("../evaluation/jmol/lib");
  }

  @Test
  public void testTrace() throws Exception {
    CS.NCFA = 0;
    Map<String, String> start = new HashMap<String, String>();
    start.put("plotLine\\(", "this");
    start.put("Cylinder3D, render\\(", "this");
    start.put("plotLineClipped\\(I", "zbuf");
    // start.put("SticksRenderer.*render.*", "this");

    String entryClass = "Lorg/openscience/jmol/app/Jmol";
    String mainMethod = MAIN_METHOD;

    runJmol(start, entryClass, mainMethod, true);
  }

  @Test
  public void testTraceMock() throws Exception {
    CS.NCFA = 1;
    Map<String, String> start = new HashMap<String, String>();
    start.put("plotLine\\(", "this");
    start.put("Cylinder3D, render\\(", "this");
    start.put("plotLineClipped\\(I", "zbuf");
    // start.put("SticksRenderer.*render.*", "this");

    String entryClass = "Lracefix/jmol/JmolEntryClass";
    String entryMethod = "testJmolEntryMethod()V";
    runJmol(start, entryClass, entryMethod, true);
  }

  private void runJmol(Map<String, String> traceStartingPoint, String entryClass, String entryMethod,
      boolean printGraphs) throws Exception {
    setup(entryClass, entryMethod);
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

    printAccessTraces(traces, name.getMethodName());

    if (printGraphs) {
      Graph<Object> prunedHeapGraph = GraphSlicer.prune(a.heapGraph, new JmolHeapGraphFilter(traces));
      Graph<CGNode> prunedCallGraph = GraphSlicer.prune(a.callGraph, new JmolCallGraphFilter());
      ColoredHeapGraphNodeDecorator color = new ColoredHeapGraphNodeDecorator(prunedHeapGraph, new AccessTraceFilter(
          traces));
      a.dotGraph(prunedHeapGraph, name.getMethodName() + "_heapGraph", color);
      // a.dotGraph(prunedCallGraph, name.getMethodName() + "_callGraph", new CGNodeDecorator(a));
    }
  }

  private static void printAccessTraces(AccessTrace[] traces, String fileName) {
    try {
      for (AccessTrace trace : traces) {
        PrintUtil.writeLCDs(trace.getTestString(), fileName + "TestString.txt");
        PrintUtil.writeLCDs(trace.getinstances().toString(), fileName + "Instances.txt");
        PrintUtil.writeLCDs(trace.getPointers().toString(), fileName + "Pointers.txt");
      }
    } catch (Exception e) {
    }
  }

}
