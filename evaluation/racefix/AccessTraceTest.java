package racefix;

import java.io.IOException;

import static junit.framework.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;
import sabazios.util.wala.viz.ColoredHeapGraphNodeDecorator;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.collections.IndiscriminateFilter;

@SuppressWarnings("deprecation")
public class AccessTraceTest extends DataRaceAnalysisTest {

  @Rule
  public TestName name = new TestName();

  private final boolean printGraphs = false;

  public AccessTraceTest() {
    super();
    this.addBinaryDependency("bin/racefix");
    this.addBinaryDependency("../lib/parallelArray.mock");
  }

  private void runTest(String startVariableName, String expected) throws ClassHierarchyException, CancelException,
      IOException {
    runTest(startVariableName, name.getMethodName(), expected);
  }

  private void runTest(String startVariableName, String raceMethod, String expected) throws ClassHierarchyException,
      CancelException, IOException {
    runTest(startVariableName, raceMethod, expected, new IndiscriminateFilter<CGNode>());
  }

  private void runTest(String startVariableName, String raceMethod, String expected, Filter<CGNode> filter)
      throws ClassHierarchyException, CancelException, IOException {
    String testString;
    final String methodName = name.getMethodName();

    setup("Lracefix/TraceSubject", methodName + "()V");
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();
    CGNode cgNode = a.findNodes(".*" + raceMethod + ".*").get(0);
    int value = U.getValueForVariableName(cgNode, startVariableName);
    System.out.println(cgNode + "" + value);
    final AccessTrace trace = new AccessTrace(a, cgNode, value, filter);
    trace.compute();
    testString = trace.getTestString();

    if (printGraphs) {
      a.dotGraph(a.heapGraph, methodName + "_HP", new ColoredHeapGraphNodeDecorator(a.heapGraph, new Filter<Object>() {

        @Override
        public boolean accepts(Object o) {
          if (trace.getinstances().contains(o))
            return true;

          if (trace.getPointers().contains(o))
            return true;

          return false;
        }

      }));
    }

    assertEquals(expected, testString);
  }

  @Test
  public void simple1() throws Exception {
    String startVariableName = "b";
    String expected = "O:TraceSubject.simple1-new TraceSubject$Dog\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simpleLabel() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleLabel-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleLabel-new TraceSubject$Dog\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simpleLabel1() throws Exception {
    String startVariableName = "pufi";
    String expected = "O:TraceSubject.simpleLabel1-new TraceSubject$Cat\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simpleTwoLabelsDeep() throws Exception {
    String startVariableName = "fifi";
    String expected = "IFK:TraceSubject$Cat.follows\n" + "IFK:TraceSubject$Dog.chases\n"
        + "O:TraceSubject.simpleTwoLabelsDeep-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleTwoLabelsDeep-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleTwoLabelsDeep-new TraceSubject$Dog\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleWithUninteresting() throws Exception {
    String startVariableName = "fifi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleWithUninteresting-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleWithUninteresting-new TraceSubject$Dog\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simplePhi() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "IFK:TraceSubject$Dog.loves\n"
        + "O:TraceSubject.simplePhi-new TraceSubject$Cat\n" + "O:TraceSubject.simplePhi-new TraceSubject$Dog\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void notSoSimplePhi() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "IFK:TraceSubject$Dog.loves\n"
        + "O:TraceSubject.notSoSimplePhi-new TraceSubject$Cat\n"
        + "O:TraceSubject.notSoSimplePhi-new TraceSubject$Dog\n"
        + "O:TraceSubject.notSoSimplePhi-new TraceSubject$Cat\n";
    runTest(startVariableName, expected);
  }

  @Test
  public void simpleCalls() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleCalls-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleCalls-new TraceSubject$Dog\n";

    runTest(startVariableName, "writeField", expected);
  }

  @Test
  public void simpleCalls2() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleCalls2-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleCalls2-new TraceSubject$Dog\n";

    runTest(startVariableName, "writeField2", expected);
  }

  @Test
  public void simpleCalls3() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleCalls3-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleCalls3-new TraceSubject$Dog\n";

    runTest(startVariableName, "writeField", expected);
  }

  @Test
  public void simpleCalls4() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleCalls4-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleCalls4-new TraceSubject$Dog\n";

    runTest(startVariableName, "writeField4", expected);
  }

  @Test
  public void simpleWithReturn() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleWithReturn-new TraceSubject$Cat\n"
        + "O:TraceSubject.makeDog-new TraceSubject$Dog\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleWithReturn2() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleWithReturn2-new TraceSubject$Cat\n"
        + "O:TraceSubject.makeDog-new TraceSubject$Dog\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleWithReturn3() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleWithReturn3-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleWithReturn3-new TraceSubject$Dog\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleWithFieldWrites() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleWithFieldWrites-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleWithFieldWrites-new TraceSubject$Dog\n"
        + "O:TraceSubject.simpleWithFieldWrites-new TraceSubject$Cat\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleWithFieldWrites2() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleWithFieldWrites2-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleWithFieldWrites2-new TraceSubject$Dog\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleWithFieldWrites3() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Cat.follows\n" + "IFK:TraceSubject$Dog.chases\n"
        + "O:TraceSubject.simpleWithFieldWrites3-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleWithFieldWrites3-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleWithFieldWrites3-new TraceSubject$Dog\n"
        + "O:TraceSubject.simpleWithFieldWrites3-new TraceSubject$Cat\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleRecursiveInternal() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "O:TraceSubject.simpleRecursiveInternal-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleRecursiveInternal-new TraceSubject$Dog\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleRecursiveInternal2() throws Exception {
    String startVariableName = "pufi";
    String expected = "O:TraceSubject.simpleRecursiveInternal2-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleRecursiveInternal2-new TraceSubject$Cat\n";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleRecursiveExternal() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Dog.chases\n" + "IFK:TraceSubject$Dog.chases\n"
        + "O:TraceSubject.simpleRecursiveExternal-new TraceSubject$Cat\n"
        + "O:TraceSubject.recurse-new TraceSubject$Dog\n" + "O:TraceSubject.recurse-new TraceSubject$Dog\n" + "";

    runTest(startVariableName, expected);
  }

  @Test
  public void simpleFilter() throws Exception {
    String startVariableName = "pufi";
    String expected = "IFK:TraceSubject$Cat.follows\n" + "O:TraceSubject.simpleFilter-new TraceSubject$Cat\n"
        + "O:TraceSubject.simpleFilter-new TraceSubject$Cat\n";

    runTest(startVariableName, "blablabla", expected, new Filter<CGNode>() {
      @Override
      public boolean accepts(CGNode n) {
        return n.getMethod().getName().toString().contains("blablabla");
      }

    });
  }

  @Test
  public void simpleInheritance() throws Exception {
    String startVariableName = "food";
    String expected = "IFK:TraceSubject$Animal.food\n" + "O:TraceSubject.simpleInheritance-new Object\n"
        + "O:TraceSubject.simpleInheritance-new TraceSubject$Dog\n";

    runTest(startVariableName, expected);
  }

}