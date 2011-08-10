package racefix;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.util.CancelException;

public class StatementOrderTest extends DataRaceAnalysisTest {

  @Rule
  public TestName name = new TestName();

  public StatementOrderTest() {
    this.addBinaryDependency("racefix");
  }

  private void runTest(String startNodeName, String endNodeName, boolean expected) throws ClassHierarchyException,
      IllegalArgumentException, CancelException, IOException {
    runTest("Lracefix/StatementOrderSubject", startNodeName, endNodeName, "age", "gender", expected);
  }

  private void runTest(String entryClassName, String startNodeName, String endNodeName, String firstInstructionName,
      String secondInstructionName, boolean expectedResult) throws ClassHierarchyException, IllegalArgumentException,
      CancelException, IOException {
    String methodName = name.getMethodName();

    setup(entryClassName, methodName + "()V");
    A a = new A(callGraph, pointerAnalysis);
    List<CGNode> startNodes = a.findNodes(".*" + startNodeName + ".*");
    SSAPutInstruction startInstr = findInstructionForName(firstInstructionName, startNodes);

    List<CGNode> endNodes = a.findNodes(".*" + endNodeName + ".*");
    SSAPutInstruction endInstr = findInstructionForName(secondInstructionName, endNodes);
    StatementOrder order = new StatementOrder(callGraph, startNodes.get(0), startInstr, endNodes.get(0), endInstr);
    assertEquals(expectedResult, order.happensBefore());
    StatementOrder reverseOrder = new StatementOrder(callGraph, endNodes.get(0), endInstr, startNodes.get(0),
        startInstr);
    assertEquals(!expectedResult, reverseOrder.happensBefore());
  }

  private SSAPutInstruction findInstructionForName(String string, List<CGNode> startNodes) {
    SSAPutInstruction startInstr = null;

    SSAInstruction[] instructions = startNodes.get(0).getIR().getInstructions();
    for (SSAInstruction ssaInstruction : instructions) {
      if (ssaInstruction instanceof SSAPutInstruction) {
        SSAPutInstruction put = (SSAPutInstruction) ssaInstruction;
        if (put.getDeclaredField().getName().toString().contains(string)) {
          startInstr = put;
          break;
        }
      }
    }
    return startInstr;
  }

  @Test
  public void testTrueIntraprocedural() throws Exception {
    runTest(name.getMethodName(), name.getMethodName(), true);
  }

  @Test
  public void testFalseIntraprocedural() throws Exception {
    runTest(name.getMethodName(), name.getMethodName(), false);
  }

  @Test
  public void testTrueInterprocedural() throws Exception {
    runTest(name.getMethodName(), "setGender", true);
  }

  @Test
  public void testFalseInterprocedural() throws Exception {
    runTest(name.getMethodName(), "setGender", false);
  }

  @Test
  public void testTrueOnReturnPath() throws Exception {
    runTest("setAge", "setGender", true);
  }

  @Test
  public void testFalseOnReturnPath() throws Exception {
    runTest("setAge", "setGender", false);
  }

  @Test
  public void testRecursiveIntraprocedural() throws Exception {
    runTest(name.getMethodName(), "setGender", true);
  }

  @Test
  public void testRecursiveInterprocedural() throws Exception {
    runTest(name.getMethodName(), "recursiveGenderSet", true);
  }

  @Test
  public void testRunSphere3D() throws Exception {
    runTest("Lracefix/StatementOrderSphere3D", "uniqueStartMethodName", "uniqueSecondMethodName", "fakeLCDField",
        "uniqueFakeLCDInstructionName", true);
  }

}
