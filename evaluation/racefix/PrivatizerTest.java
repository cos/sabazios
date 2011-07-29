package racefix;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.A;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentAccesses;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.Loop;
import sabazios.domains.Loops;
import sabazios.tests.DataRaceAnalysisTest;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.util.CancelException;

public class PrivatizerTest extends DataRaceAnalysisTest {

  @Rule
  public TestName name = new TestName();
  private Privatizer privatizer;
  private String startNodeName = "coordX";

  public PrivatizerTest() {
    this.addBinaryDependency("racefix");
    this.addBinaryDependency("../lib/parallelArray.mock");
  }

  @Before
  public void beforeTest() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
    foundCA = findCA("Lracefix/PrivatizerSubject", name.getMethodName() + "()V");
    Set<Loop> keySet = foundCA.keySet();
    Loop outer = null;
    for (Loop loop : keySet) {
      outer = loop;
    }

    Set<ConcurrentFieldAccess> accesses = (Set<ConcurrentFieldAccess>) foundCA.get(outer);

    privatizer = new Privatizer(a, accesses);
    privatizer.compute();
  }

  private SSAPutInstruction findInstructionForName(String string, List<CGNode> startNodes) {
    SSAPutInstruction startInstr = null;

    for (CGNode cgNode : startNodes) {

      System.out.println("\n" + cgNode + "\n");

      SSAInstruction[] instructions = cgNode.getIR().getInstructions();// startNodes.get(0).getIR().getInstructions();
      for (SSAInstruction ssaInstruction : instructions) {
        System.out.println(ssaInstruction);
        if (ssaInstruction instanceof SSAPutInstruction) {
          SSAPutInstruction put = (SSAPutInstruction) ssaInstruction;
          if (put.getDeclaredField().getName().toString().contains(string)) {
            startInstr = put;
            break;
          }
        }
      }
    }
    return startInstr;
  }

  @Test
  public void simpleRace() throws Exception {
    String expectedLCD = "";
    String expectedNoLCD = "Object : racefix.PrivatizerSubject.simpleRace(PrivatizerSubject.java:22) new PrivatizerSubject$PrivatizableParticle\n"
        + "   Alpha accesses:\n"
        + "     Write racefix.PrivatizerSubject$1.op(PrivatizerSubject.java:27) - .coordX\n"
        + "   Beta accesses:\n" + "     Write racefix.PrivatizerSubject$1.op(PrivatizerSubject.java:27) - .coordX\n";
    TestCase.assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
    TestCase.assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
  }

  @Test
  public void writeReadRace() throws Exception {
    String expectedLCD = "";
    String expectedNoLCD = "Object : racefix.PrivatizerSubject.writeReadRace(PrivatizerSubject.java:36) new PrivatizerSubject$PrivatizableParticle\n"
        + "   Alpha accesses:\n"
        + "     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:41) - .coordX\n"
        + "   Beta accesses:\n"
        + "     Read racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:42) - .coordX\n"
        + "     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:41) - .coordX\n";
    TestCase.assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
    TestCase.assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
  }

  @Test
  public void readWriteRace() throws Exception {
    String expectedLCD = "Object : racefix.PrivatizerSubject.writeReadRace(PrivatizerSubject.java:36) new PrivatizerSubject$PrivatizableParticle\n"
        + "   Alpha accesses:\n"
        + "     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:41) - .coordX\n"
        + "   Beta accesses:\n"
        + "     Read racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:42) - .coordX\n"
        + "     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:41) - .coordX\n";
    String expectedNoLCD = "";
    TestCase.assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
    TestCase.assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
  }
}
