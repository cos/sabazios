package racefix;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

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

  public void runTest(String startNodeName, String endNodeName, boolean expected) throws ClassHierarchyException,
      IllegalArgumentException, CancelException, IOException {
    String methodName = name.getMethodName();
    SSAPutInstruction startInstr = null;

    setup("Lracefix/StatementOrderSubject", methodName + "()V");
    A a = new A(callGraph, pointerAnalysis);
    List<CGNode> startNodes = a.findNodes(".*" + startNodeName + ".*");
    startInstr = findInstructionForName("age", startNodes);

    List<CGNode> endNodes = a.findNodes(".*" + endNodeName + ".*");
    SSAPutInstruction endInstr = findInstructionForName("gender", endNodes);
    StatementOrder order = new StatementOrder(callGraph, startNodes.get(0), startInstr, endNodes.get(0), endInstr);
    Assert.assertEquals(expected, order.happensBefore());
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

}
