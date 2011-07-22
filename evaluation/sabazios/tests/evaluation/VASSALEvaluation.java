package sabazios.tests.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.collections.IndiscriminateFilter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.GraphUtil;
import com.ibm.wala.util.warnings.WalaException;

import racefix.AccessTrace;
import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;
import sabazios.util.wala.viz.NodeDecorator;

public class VASSALEvaluation extends DataRaceAnalysisTest {

  public VASSALEvaluation() {
    addBinaryDependency("../vassal/bin");
    addBinaryDependency("../lib/parallelArray.mock");
    addJarFolderDependency("../vassal/lib");
    addJarFolderDependency("../vassal/lib-nondist");
  }

  @Test
  public void test() throws Exception {
    String racyMethodSignature = "zoom(Ljava/awt/image/WritableRaster;Ljava/awt/Rectangle;Ljava/awt/image/BufferedImage;LVASSAL/tools/image/GeneralFilter$Filter;)V";
    setup(
        "LVASSAL/tools/image/GeneralFilter",
        racyMethodSignature);

    A a = new A(callGraph, pointerAnalysis);
    a.precompute();
    
    List<CGNode> foundNodes = a.findNodes(".*" + "zoom" + ".*");
    CGNode cgNode = foundNodes.get(0);
    int value = U.getValueForVariableName(cgNode, "work");
    
    AccessTrace trace = new AccessTrace(a, cgNode, value, new IndiscriminateFilter<CGNode>());
    trace.compute();
    
    final List<String> strings = new ArrayList<String>();
    LinkedHashSet<InstanceKey> instances = trace.getinstances();
    for (InstanceKey instanceKey : instances) {
      strings.add(instanceKey.toString());
    }
    LinkedHashSet<PointerKey> pointers = trace.getPointers();
    for (PointerKey pointerKey : pointers) {
      strings.add(pointerKey.toString());
    }
    
    NodeDecorator decorator = new NodeDecorator() {
      
      @Override
      public String getLabel(Object o) throws WalaException {
        String init = o.toString();
        String fin = init;
        if (strings.contains(o.toString())) {
          fin = "{MARKED}  " + init;
          System.out.println(o.toString());
        }
        return fin;
      }

      @Override
      public String getDecoration(Object n) {
        // TODO Auto-generated method stub
        return null;
      }
    };
    
    Graph<Object> prunedHP = GraphSlicer.prune(a.heapGraph, new Filter<Object>() {

      @Override
      public boolean accepts(Object o) {
        return o.toString().contains("zoom") && o.toString().contains("GeneralFilter");
      }

    });

    a.dotGraph(prunedHP, "VASSALEvaluation" + "_HP", decorator);
  }
}
