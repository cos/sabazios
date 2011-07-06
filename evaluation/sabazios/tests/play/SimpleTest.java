package sabazios.tests.play;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import sabazios.A;
import sabazios.tests.DataRaceAnalysisTest;
import sabazios.util.wala.viz.PDFViewUtil;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.warnings.WalaException;
import com.sun.org.apache.bcel.internal.generic.AALOAD;

public class SimpleTest extends DataRaceAnalysisTest {

	public SimpleTest() {
		super();
		this.addBinaryDependency("sandbox");
	}
	
	@Test
	public void verySimple() throws CancelException {
		try {
	    setup("Lsandbox/Bar", "foo()V");
	    A a = new A(callGraph, pointerAnalysis, builder);
	    List<CGNode> nodes = a.findNodes(".*foo.*");	    
	    CGNode cgNode = nodes.get(0);
//			PDFViewUtil.ghostviewIR(A.cha, cgNode, "debug/irPdf" + cgNode.getGraphNodeId(), "./debug/irDot"
//					+ cgNode.getGraphNodeId(), "/opt/local/bin/dot", "open");
//	    
	    IR ir = cgNode.getIR();
	    SSACFG cfg = ir.getControlFlowGraph();
//	    SSAInstruction[] instructions = cfg.getInstructions();
//	    for (SSAInstruction ssaInstruction : instructions) {
//	    		System.out.println(ssaInstruction);
//      }
	    
	    BasicBlock entry = cfg.entry();
	    visit(cfg, entry);
	    
    } catch (ClassHierarchyException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } catch (IllegalArgumentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    } catch (WalaException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
	}

	Set<ISSABasicBlock> visited = new LinkedHashSet<ISSABasicBlock>();
	private void visit(SSACFG cfg, ISSABasicBlock node) {
		if(visited.contains(node))
			return;
		visited.add(node);
		System.out.println(node);
		Iterator<SSAInstruction> iterator = node.iterator();
		while (iterator.hasNext()) {
      SSAInstruction ssaInstruction = (SSAInstruction) iterator.next();
      System.out.println("      "+ssaInstruction);
    }
		Iterator<ISSABasicBlock> succNodes = cfg.getSuccNodes(node);
		while (succNodes.hasNext()) {
	    ISSABasicBlock issaBasicBlock = (ISSABasicBlock) succNodes.next();
	    visit(cfg, issaBasicBlock);
    }
  }

	@Test
	public void highCFA() throws CancelException {
//		Set<Race> races = findRaces("Lsubjects/HighCFA", "main()V");
		
	}

}
