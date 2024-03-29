/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package sabazios.util.wala.viz;

import java.util.HashMap;
import java.util.Iterator;

import sabazios.A;

import com.ibm.wala.cfg.CFGSanitizer;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSACFG.ExceptionHandlerBasicBlock;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.util.warnings.WalaException;

/**
 * utilities for integrating with ghostview (or another PS/PDF viewer)
 */
public class PDFViewUtil {

/**
   * spawn a process to view a WALA IR
   * 
   * @return a handle to the ghostview process
   */
  public static Process ghostviewIR(IClassHierarchy cha, CGNode n, String pdfFile, String dotFile, String dotExe, String pdfViewExe)
      throws WalaException {
    return ghostviewIR(cha, n, pdfFile, dotFile, dotExe, pdfViewExe, null);
  }

  /**
   * spawn a process to view a WALA IR
   * 
   * @return a handle to the pdf viewer process
   * @throws IllegalArgumentException if ir is null
   */
  public static Process ghostviewIR(IClassHierarchy cha, CGNode n, String pdfFile, String dotFile, String dotExe, String pdfViewExe,
      NodeDecorator annotations) throws WalaException {

    if (n.getIR() == null) {
      throw new IllegalArgumentException("ir is null");
    }
    Graph<? extends ISSABasicBlock> g = n.getIR().getControlFlowGraph();

    NodeDecorator labels = makeIRDecorator(n);
    if (annotations != null) {
      labels = new ConcatenatingNodeDecorator(annotations, labels);
    }

    g = CFGSanitizer.sanitize(n.getIR(), cha);

    DotUtil.dotify(g, labels, n.getIR().getMethod().toString(), dotFile, pdfFile, dotExe);

    return launchPDFView(pdfFile, pdfViewExe);
  }

  public static NodeDecorator makeIRDecorator(CGNode n) {
    if (n.getIR() == null) {
      throw new IllegalArgumentException("ir is null");
    }
    final HashMap<BasicBlock, String> labelMap = HashMapFactory.make();
    for (Iterator it = n.getIR().getControlFlowGraph().iterator(); it.hasNext();) {
      SSACFG.BasicBlock bb = (SSACFG.BasicBlock) it.next();
      labelMap.put(bb, getNodeLabel(n, bb));
    }
    NodeDecorator labels = new NodeDecorator() {
      public String getLabel(Object o) {
        return labelMap.get(o);
      }

	@Override
	public String getDecoration(Object n) {
		return "";
	}

	@Override
	public boolean shouldDisplay(Object n) {
		return true;
	}
    };
    return labels;
  }

  /**
   * A node decorator which concatenates the labels from two other node decorators
   */
  private final static class ConcatenatingNodeDecorator implements NodeDecorator {

    private final NodeDecorator a;

    private final NodeDecorator b;

    ConcatenatingNodeDecorator(NodeDecorator A, NodeDecorator B) {
      this.a = A;
      this.b = B;
    }

    public String getLabel(Object o) throws WalaException {
      return a.getLabel(o) + b.getLabel(o);
    }

	@Override
	public String getDecoration(Object n) {
		return "";
	}

	@Override
	public boolean shouldDisplay(Object n) {
		return true;
	}

  }

  private static String getNodeLabel(CGNode n, BasicBlock bb) {
	  IR ir = n.getIR();
    StringBuffer result = new StringBuffer();

    int start = bb.getFirstInstructionIndex();
    int end = bb.getLastInstructionIndex();
    result.append("BB").append(bb.getNumber());
    if (bb.isEntryBlock()) {
      result.append(" (en)\\n");
    } else if (bb.isExitBlock()) {
      result.append(" (ex)\\n");
    }
    if (bb instanceof ExceptionHandlerBasicBlock) {
      result.append("<Handler>");
    }
    result.append("\\n");
    for (Iterator it = bb.iteratePhis(); it.hasNext();) {
      SSAPhiInstruction phi = (SSAPhiInstruction) it.next();
      if (phi != null) {
        result.append("           " + phi.toString(ir.getSymbolTable())).append("\\l");
      }
    }
    if (bb instanceof ExceptionHandlerBasicBlock) {
      ExceptionHandlerBasicBlock ebb = (ExceptionHandlerBasicBlock) bb;
      SSAGetCaughtExceptionInstruction s = ebb.getCatchInstruction();
      if (s != null) {
        result.append("           " + s.toString(ir.getSymbolTable())).append("\\l");
      } else {
        result.append("           " + " No catch instruction. Unreachable?\\l");
      }
    }
    SSAInstruction[] instructions = ir.getInstructions();
    for (int j = start; j <= end; j++) {
      if (instructions[j] != null) {
        StringBuffer x = new StringBuffer(j + "   " + instructions[j].toString(ir.getSymbolTable()));
        StringStuff.padWithSpaces(x, 35);
        result.append(x);
//        result.append(" "+a.locks.get(n, instructions[j]));
        result.append("\\l");
      }
    }
    for (Iterator it = bb.iteratePis(); it.hasNext();) {
      SSAPiInstruction pi = (SSAPiInstruction) it.next();
      if (pi != null) {
        result.append("           " + pi.toString(ir.getSymbolTable())).append("\\l");
      }
    }
    return result.toString();
  }

  /**
   * Launch a process to view a PDF file
   */
  public static Process launchPDFView(String pdfFile, String gvExe) throws WalaException {
    // set up a viewer for the ps file.
    if (gvExe == null) {
      throw new IllegalArgumentException("null gvExe");
    }
    if (pdfFile == null) {
      throw new IllegalArgumentException("null psFile");
    }
    final PDFViewLauncher gv = new PDFViewLauncher();
    gv.setGvExe(gvExe);
    gv.setPDFFile(pdfFile);
    gv.run();
    if (gv.getProcess() == null) {
      throw new WalaException(" problem spawning process ");
    }
    return gv.getProcess();
  }

}
