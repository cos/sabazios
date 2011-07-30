package racefix;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.Loop;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.wala.viz.ColoredHeapGraphNodeDecorator;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;

public class PrivatizerTest extends DataRaceAnalysisTest {

  @Rule
  public TestName name = new TestName();
  private Privatizer privatizer;

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

  @After
  public void afterTest() throws Exception {
    HeapGraph heapGraph = a.heapGraph;
    a.dotGraph(heapGraph, testClassName, new ColoredHeapGraphNodeDecorator(heapGraph, new Filter<Object>() {

      @Override
      public boolean accepts(Object o) {
        return privatizer.getFieldNodesToPrivatize().contains(o) || privatizer.getInstancesToPrivatize().contains(o);
      }

    }));

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
    String expectedNoLCD = "Object : racefix.PrivatizerSubject.simpleRace(PrivatizerSubject.java:25) new PrivatizerSubject$Particle\n"
        + "   Alpha accesses:\n"
        + "     Write racefix.PrivatizerSubject$1.op(PrivatizerSubject.java:30) - .coordX\n"
        + "   Beta accesses:\n" + "     Write racefix.PrivatizerSubject$1.op(PrivatizerSubject.java:30) - .coordX\n";
    assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
    assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
  }

  @Test
  public void writeReadRace() throws Exception {
    String expectedLCD = "";
    String expectedNoLCD = "Object : racefix.PrivatizerSubject.writeReadRace(PrivatizerSubject.java:40) new PrivatizerSubject$Particle\n"
        + "   Alpha accesses:\n"
        + "     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:45) - .coordX\n"
        + "   Beta accesses:\n"
        + "     Read racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:46) - .coordX\n"
        + "     Write racefix.PrivatizerSubject$2.op(PrivatizerSubject.java:45) - .coordX\n";
    assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
    assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
  }

  @Test
  public void readWriteRace() throws Exception {
    String expectedLCD = "Object : racefix.PrivatizerSubject.readWriteRace(PrivatizerSubject.java:56) new PrivatizerSubject$Particle\n"
        + "   Alpha accesses:\n"
        + "     Write racefix.PrivatizerSubject$3.op(PrivatizerSubject.java:62) - .coordX\n"
        + "   Beta accesses:\n"
        + "     Write racefix.PrivatizerSubject$3.op(PrivatizerSubject.java:62) - .coordX\n"
        + "     Read racefix.PrivatizerSubject$3.op(PrivatizerSubject.java:61) - .coordX\n";
    String expectedNoLCD = "";
    assertEquals(expectedLCD, privatizer.getAccessesInLCDTestString());
    assertEquals(expectedNoLCD, privatizer.getAccessesNotInLCDTestString());
  }

  @Test
  public void fieldSuperstar() throws Exception {
    assertEquals(
        "[< Application, Lracefix/PrivatizerSubject$Particle, next, <Application,Lracefix/PrivatizerSubject$Particle> >]",
        privatizer.getStarredFields());
  }
}