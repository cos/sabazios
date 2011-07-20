package racefix;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import sabazios.A;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentAccesses;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.ConcurrentFieldAccesses;
import sabazios.domains.FieldAccess;
import sabazios.domains.ObjectAccess;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.U;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

public class PrivatizerTest extends DataRaceAnalysisTest { 

  @Rule
  public TestName name = new TestName();

  public PrivatizerTest() {
    this.addBinaryDependency("racefix");
    this.addBinaryDependency("../lib/parallelArray.mock");
  }

  public void runTest(String startNodeName) throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
    setup("Lracefix/PrivatizerSubject", name.getMethodName() + "()V");
    
    A a = new A(callGraph, pointerAnalysis);
    a.precompute();
    
    List<CGNode> startNodes = a.findNodes(".*" + "op" + ".*");
    SSAPutInstruction startInstr = findInstructionForName(startNodeName, startNodes);
    
    int value = U.getValueForVariableName(startNodes.get(0), startNodeName);
    LocalPointerKey lpk = a.pointerForValue.get(startNodes.get(0), value);
    Iterator<Object> succNodes = a.heapGraph.getSuccNodes(lpk);
    
    InstanceKey ik = (InstanceKey) succNodes.next();
    
    FieldAccess o1 = new FieldAccess(a, startNodes.get(0), startInstr, ik, null);
    
    value = U.getValueForVariableName(startNodes.get(0), startNodeName);
    FieldAccess o2 = new FieldAccess(a, startNodes.get(1), startInstr, (InstanceKey) succNodes.next(), null);
    
    ConcurrentFieldAccess access = new ConcurrentFieldAccess(null, ik, null);
    access.alphaAccesses.add(o1);
    access.betaAccesses.add(o2);
    
    
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
  public void simpleRace() throws Exception {
    runTest("coordX");
  }

}
