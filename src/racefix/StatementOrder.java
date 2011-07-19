package racefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sabazios.util.CodeLocation;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class StatementOrder {

  private CallGraph callGraph;
  private final SSAInstruction i2;
  private final CGNode n1;
  private final SSAInstruction i1;
  private final CGNode n2;

  public StatementOrder(CallGraph callGraph, CGNode n1, SSAInstruction i1, CGNode n2, SSAInstruction i2) {
    this.callGraph = callGraph;
    this.n1 = n1;
    this.i1 = i1;
    this.n2 = n2;
    this.i2 = i2;
  }

  public boolean happensBefore() {
    return visit(n1, i1);
  }

  private boolean visit(CGNode n, SSAInstruction i) {
    SSACFG cfg = n.getIR().getControlFlowGraph();

    int ssaInstructionNo = CodeLocation.getSSAInstructionNo(n, i);
    BasicBlock block = cfg.getBlockForInstruction(ssaInstructionNo);
    List<SSAInstruction> allInstructions = block.getAllInstructions();
    List<SSAInstruction> instructionsAfter = new ArrayList<SSAInstruction>();
    for (Iterator iterator = allInstructions.iterator(); iterator.hasNext();) {
      SSAInstruction ssaInstruction = (SSAInstruction) iterator.next();
      if (ssaInstruction.equals(i) || !instructionsAfter.isEmpty())
        instructionsAfter.add(i);
    }

    return visitInstructions(n, instructionsAfter) || visitNextBlocks(n, block);
  }

  private Map<CGNode, Set<BasicBlock>> map = new HashMap<CGNode, Set<BasicBlock>>();

  private boolean visit(CGNode n, BasicBlock block) {

    Set<BasicBlock> blockSet = map.get(n);
    if (blockSet == null) {
      blockSet = new LinkedHashSet<BasicBlock>();
      blockSet.add(block);
      map.put(n, blockSet);
    } else if (!blockSet.contains(block))
      blockSet.add(block);
    else
      return false;

    List<SSAInstruction> allInstructions = block.getAllInstructions();
    return visitInstructions(n, allInstructions) || visitNextBlocks(n, block);
  }

  private boolean visitNextBlocks(CGNode n, BasicBlock block) {
    SSACFG cfg = n.getIR().getControlFlowGraph();
    Iterator<ISSABasicBlock> succNodes = cfg.getSuccNodes(block);
    while (succNodes.hasNext()) {
      ISSABasicBlock issaBasicBlock = (ISSABasicBlock) succNodes.next();
      if (visit(n, (BasicBlock) issaBasicBlock))
        return true;
    }
    return false;
  }

  private boolean visitInstructions(CGNode n, List<SSAInstruction> allInstructions) {
    for (SSAInstruction ssaInstruction : allInstructions) {
      if (n.equals(n2) && ssaInstruction.equals(i2)) {
        return true;
      }

      if (ssaInstruction instanceof SSAInvokeInstruction) {
        SSAInvokeInstruction invoke = (SSAInvokeInstruction) ssaInstruction;
        CallSiteReference callSite = invoke.getCallSite();
        Set<CGNode> possibleTargets = callGraph.getPossibleTargets(n, callSite);
        for (CGNode cgNode : possibleTargets) {
          BasicBlock entryBlock = cgNode.getIR().getControlFlowGraph().entry();
          if (visit(cgNode, entryBlock))
            return true;
        }
        return visit(n, ssaInstruction);
      }
    }
    return false;
  }
}
