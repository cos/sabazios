package racefix;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runners.JUnit4;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;

public class AccessTraceTest extends DataRaceAnalysisTest {

  public AccessTraceTest() {
    super();
    this.addBinaryDependency("racefix");
    this.addBinaryDependency("../lib/parallelArray.mock");
  }

  @Test
  public void test() throws Exception {
    setup("Lracefix/Foo", "simple1()V");
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();
    CGNode cgNode = a.findNodes(".*simple1.*").get(0);
    System.out.println(cgNode);
    AccessTrace trace = new AccessTrace(a, cgNode, 3);
    trace.compute();
    String testString = trace.getTestString();
    Assert.assertEquals("LPK:Foo.simple1-v3-b\n" + "O:Foo.simple1-new Foo$Dog\n", testString);

    Graph<Object> prunedHP = GraphSlicer.prune(a.heapGraph, new Filter<Object>() {

      @Override
      public boolean accepts(Object o) {
        return o.toString().contains("simple1");
      }

    });

    a.dotGraph(prunedHP, "test_dot", null);

  }
}