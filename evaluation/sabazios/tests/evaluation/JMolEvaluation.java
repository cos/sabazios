package sabazios.tests.evaluation;

import java.util.List;

import org.junit.Test;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.collections.IndiscriminateFilter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

import racefix.AccessTrace;
import sabazios.A;
import sabazios.CGNodeDecorator;
import sabazios.ColoredHeapGraphNodeDecorator;
import sabazios.HeapGraphNodeDecorator;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.Log;
import sabazios.util.U;
import sabazios.wala.CS;
import sabazios.wala.WalaAnalysis;

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
    // setup("Lorg/jmol/shape/SticksRenderer", "render()V");
    setup("Lorg/openscience/jmol/app/Jmol", MAIN_METHOD);
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();

    List<CGNode> sticksRend = a.findNodes(".*" + "renderBond" + ".*");
    List<CGNode> line3D = a.findNodes(".*" + "plotLine\\(" + ".*");
    CGNode renderBond = sticksRend.get(0);
    CGNode plotLine = line3D.get(0);

    final AccessTrace trace = new AccessTrace(a, renderBond, 1, new IndiscriminateFilter<CGNode>());
    final AccessTrace traceLine = new AccessTrace(a, plotLine, 1, new IndiscriminateFilter<CGNode>());
    trace.compute();
    traceLine.compute();

    final AccessTrace[] traces = { trace, traceLine };
    // final AccessTrace[] traces = { traceLine };

    printAccessTraces(traces);

    @SuppressWarnings("deprecation")
    Graph<Object> prunedHeapGraph = GraphSlicer.prune(a.heapGraph, new Filter<Object>() {
      @Override
      public boolean accepts(Object o) {
        return !isUnwanted(o) && isWanted(o);
      }

      private boolean isInTrace(Object o) {
        for (AccessTrace trace : traces) {
          if (trace.getinstances().contains(o) || trace.getPointers().contains(o))
            return true;
        }
        return false;
      }

      private boolean isWanted(Object o) {

        if (isInTrace(o))
          return true;

        String temp = o.toString();
        String[] str = { "SticksRenderer", "Graphics3D", "Circle3D", "Cylinder3D", "Line3D", "plotLine",
            "fillCylinder", "fillSphere", "drawDashed", "getTrimmedLine", "plotLineClipped" };
        for (String s : str) {
          if (temp.contains(s))
            return true;
        }
        return false;
      }

      private boolean isUnwanted(Object o) {
        if (o instanceof LocalPointerKey) {
          return true;
        }

        String temp = o.toString();
        String str[] = { "Exception", "Assertion", "error", "StackTrace", "Vector3f", "Point3f", "Vector3i", "Point3i",
            "BitSet", "Iterator", "HashTable", "Class", "CurrentThread", "getComponentType", "InternalError",
            "Sphere3D", "Hermite3D", "Normix3D", "Triangle3D" };

        for (String s : str) {
          if (temp.contains(s))
            return true;
        }
        return false;
      }

    });

    @SuppressWarnings("deprecation")
    Graph<CGNode> prunedCallGraph = GraphSlicer.prune(a.callGraph, new Filter<CGNode>() {
      @Override
      public boolean accepts(CGNode o) {
        return isWanted(o);
      }

      private boolean isWanted(CGNode o) {
        String temp = o.toString();
        String[] str = { "fillCylinder", "drawDashed", "getTrimmedLine", "plotLineClipped", "drawLine", "plotLine",
            "plotLineDelta" };
        for (String s : str) {
          if (temp.contains(s))
            return true;
        }
        return false;
      }

    });
    @SuppressWarnings("deprecation")
    Filter<Object> filter = new Filter<Object>() {
      @Override
      public boolean accepts(Object o) {
        for (AccessTrace trace : traces) {
          if ((trace.getinstances().contains(o) || trace.getPointers().contains(o)) == true)
            return true;
        }
        return false;
      }
    };

    ColoredHeapGraphNodeDecorator color = new ColoredHeapGraphNodeDecorator(prunedHeapGraph, filter);
    // a.dotGraph(prunedHeapGraph, "Jmol_HeapGraph", new HeapGraphNodeDecorator(prunedHeapGraph));
    a.dotGraph(prunedHeapGraph, "heapGraph_color", color);
    a.dotGraph(prunedCallGraph, "callGraph", new CGNodeDecorator(a));
  }

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
