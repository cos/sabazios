package racefix;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

public class AccessTraceTest extends DataRaceAnalysisTest {

  @Rule
  public TestName name = new TestName();

  public AccessTraceTest() {
    super();
    this.addBinaryDependency("racefix");
    this.addBinaryDependency("../lib/parallelArray.mock");
  }

  private void runTest(String startVariableName, String expected) throws ClassHierarchyException, CancelException,
      IOException {
    String testString;
    String methodName = name.getMethodName();

    setup("Lracefix/Foo", methodName + "()V");
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();
    CGNode cgNode = a.findNodes(".*" + methodName + ".*").get(0);
    int value = U.getValueForVariableName(cgNode, startVariableName);
    System.out.println(cgNode + "" + value);
    AccessTrace trace = new AccessTrace(a, cgNode, value);
    trace.compute();
    testString = trace.getTestString();
    Assert.assertEquals(expected, testString);
  }

  @Test
  public void simple1() throws Exception {
    String startVariableName = "b";
    String expected = "O:Foo.simple1-new Foo$Dog\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simpleLabel() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:Foo$Dog.chases\n" + "O:Foo.simpleLabel-new Foo$Cat\n" + "O:Foo.simpleLabel-new Foo$Dog\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simpleLabel1() throws Exception {
    String startVariableName = "pufi";
    String expected = "O:Foo.simpleLabel1-new Foo$Cat\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simpleTwoLabelsDeep() throws Exception {
    String startVariableName = "fifi";
    String expected = "IFK:Foo$Cat.follows\n" + "IFK:Foo$Dog.chases\n" + "O:Foo.simpleTwoLabelsDeep-new Foo$Cat\n"
        + "O:Foo.simpleTwoLabelsDeep-new Foo$Cat\n" + "O:Foo.simpleTwoLabelsDeep-new Foo$Dog\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleWithUninteresting() throws Exception {
    String startVariableName = "fifi";
    String expected = "IFK:Foo$Dog.chases\n" + "O:Foo.simpleWithUninteresting-new Foo$Cat\n"
        + "O:Foo.simpleWithUninteresting-new Foo$Dog\n";
    runTest(startVariableName, expected);
  }
  
  @Test
  public void simplePhi() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:Foo$Dog.chases\n" + 
    		"IFK:Foo$Dog.loves\n" + 
    		"O:Foo.simplePhi-new Foo$Cat\n" + 
    		"O:Foo.simplePhi-new Foo$Dog\n";
    runTest(startVariableName, expected);
  }
  
}