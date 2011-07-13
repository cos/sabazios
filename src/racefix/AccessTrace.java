package racefix;

import java.util.Iterator;
import java.util.LinkedHashSet;

import sabazios.A;
import sabazios.domains.PointerForValue;
import sabazios.util.CodeLocation;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.graph.traverse.BFSIterator;

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
    LocalPointerKey lpk = pv.get(n, v);
    System.out.println(lpk);
    pointers.add(lpk);
    Iterator<Object> succNodes = a.heapGraph.getSuccNodes(lpk);
    while (succNodes.hasNext()) {
      InstanceKey o = (InstanceKey) succNodes.next();
      instances.add(o);
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