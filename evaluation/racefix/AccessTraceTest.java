package racefix;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runners.JUnit4;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;

public class AccessTraceTest extends DataRaceAnalysisTest {

  public AccessTraceTest() {
    super();
    this.addBinaryDependency("racefix");
    this.addBinaryDependency("../lib/parallelArray.mock");
  }

  @Test
  public void testSimple() throws Exception {
    setup("Lracefix/Foo", "simple1()V");
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();
    CGNode cgNode = a.findNodes(".*simple1.*").get(0);
    AccessTrace trace = new AccessTrace(a, cgNode, 3);
    trace.compute();
    String testString = trace.getTestString();
    Assert.assertEquals("O:Foo.simple1-new Foo$Dog\n", testString);

    Graph<Object> prunedHP = GraphSlicer.prune(a.heapGraph, new Filter<Object>() {

      @Override
      public boolean accepts(Object o) {
        return o.toString().contains("simple1");
      }

    });

    a.dotGraph(prunedHP, "test_dot", null);

  }

  @Test
  public void testSimpleLabel() throws Exception {
    setup("Lracefix/Foo", "simpleLabel()V");
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();
    CGNode cgNode = a.findNodes(".*simpleLabel.*").get(0);
    int value = U.getValueForVariableName(cgNode, "pufi");
    System.out.println(value);
    AccessTrace trace = new AccessTrace(a, cgNode, value);
    trace.compute();
    Assert.assertEquals("IFK:Foo$Dog.chases\n" + "O:Foo.simpleLabel-new Foo$Cat\n" + "O:Foo.simpleLabel-new Foo$Dog\n",
        trace.getTestString());
  }
}