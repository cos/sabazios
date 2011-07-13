package racefix;

import java.util.Iterator;
import java.util.LinkedHashSet;

import sabazios.A;
import sabazios.domains.PointerForValue;
import sabazios.util.CodeLocation;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.FieldReference;

/**
 * 
 * @author caius
 * 
 */
public class AccessTrace {
  private final CGNode n;
  private final int v;
  private final A a;
  private LinkedHashSet<PointerKey> pointers = new LinkedHashSet<PointerKey>();
  private LinkedHashSet<InstanceKey> instances = new LinkedHashSet<InstanceKey>();
  private PointerForValue pv;

  public AccessTrace(A a, CGNode n, int v) {
    this.a = a;
    this.n = n;
    this.v = v;
    pv = a.pointerForValue;
  }

  public void compute() {
    CGNode n2 = n;
    int v2 = v;
    solveNV(n2, v2);
  }

  private void solveNV(CGNode n2, int v2) {
    LocalPointerKey lpk = pv.get(n2, v2);
    Iterator<Object> succNodes = a.heapGraph.getSuccNodes(lpk);
    while (succNodes.hasNext()) {
      InstanceKey o = (InstanceKey) succNodes.next();
      instances.add(o);

      DefUse du = n2.getDU();
      SSAInstruction def = du.getDef(v2);
      // if (def instanceof SSANewInstruction) {
      // SSANewInstruction newInstr = (SSANewInstruction) def;
      //
      // }
      if (def instanceof SSAGetInstruction) {
        SSAGetInstruction get = (SSAGetInstruction) def;
        FieldReference declaredField = get.getDeclaredField();
        Iterator<Object> pred = a.heapGraph.getPredNodes(o);
        while (pred.hasNext()) {
          Object prev = (Object) pred.next();
          if (prev instanceof InstanceFieldKey) {
            InstanceFieldKey field = (InstanceFieldKey) prev;

            if (field.getField().getReference().equals(declaredField))
              pointers.add(field);
              
            solveNV(n, get.getRef());
          }
        }
      }
    }
  }

  public String getTestString() {
    String s = "";
    // "[SITE_IN_NODE{< Application, Lracefix/Foo, simple1()V >:NEW <Application,Lracefix/Foo$Dog>@0 in Everywhere}]\n"
    // +
    // "\n" +
    // "[[Node: < Application, Lracefix/Foo, simple1()V > Context: Everywhere, v3]]"
    for (PointerKey o : pointers) {
      if (o instanceof LocalPointerKey) {
        LocalPointerKey p = (LocalPointerKey) o;
        s += "LPK:";
        s += p.getNode().getMethod().getDeclaringClass().getName().getClassName().toString();
        s += ".";
        s += p.getNode().getMethod().getName().toString();
        s += "-v";
        s += p.getValueNumber();
        String variableName = CodeLocation.variableName(n, p.getValueNumber());
        if (variableName != null)
          s += "-" + variableName;
        s += "\n";
      }

      if (o instanceof InstanceFieldKey) {
        InstanceFieldKey p = (InstanceFieldKey) o;
        s += "IFK:";
        s += p.getField().getDeclaringClass().getName().getClassName().toString();
        s += ".";
        s += p.getField().getName().toString();
        s += "\n";
      }
    }

    for (InstanceKey o : instances) {
      if (o instanceof AllocationSiteInNode) {
        AllocationSiteInNode as = (AllocationSiteInNode) o;
        s += "O:";
        s += as.getNode().getMethod().getDeclaringClass().getName().getClassName().toString();
        s += ".";
        s += as.getNode().getMethod().getName().toString();
        s += "-new ";
        s += as.getSite().getDeclaredType().getName().getClassName().toString();
        s += "\n";
      }
    }
    // return instances.toString() + "\n\n" + pointers.toString();
    return s;
  }
}