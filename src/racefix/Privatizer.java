package racefix;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.collections.IndiscriminateFilter;

import sabazios.A;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentAccesses;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.Loop;
import sabazios.domains.ReadFieldAccess;
import sabazios.domains.WriteFieldAccess;

public class Privatizer {

  private final A a;
  private final Set<ConcurrentFieldAccess> accesses;

  private final Set<ConcurrentFieldAccess> accessesNotLCD = new LinkedHashSet<ConcurrentFieldAccess>();
  private final Set<ConcurrentFieldAccess> accessesLCD = new LinkedHashSet<ConcurrentFieldAccess>();

  private final Set<InstanceFieldKey> fieldNodesToPrivatize = new LinkedHashSet<InstanceFieldKey>();
  private final Set<InstanceKey> instancesToPrivatize = new LinkedHashSet<InstanceKey>();
  
  private final Set<IField> fieldsToPrivatize = new LinkedHashSet<IField>();

  private final Set<IField> mustNotPrivatize = new LinkedHashSet<IField>();

  public Privatizer(A a, Set<ConcurrentFieldAccess> accesses) {
    this.a = a;
    this.accesses = accesses;
  }

  public Set<Object> compute() {
    markLCDAccesses();
    gatherAllPrivatizableHeapNodes();
    gatherAllMustNotPrivatizeFields();
    gatherPrivatizableFields();
    markStarredFields();
    return null;
  }

  private void markStarredFields() {
    // TODO Auto-generated method stub
  }

  private void gatherPrivatizableFields() {
    for (InstanceFieldKey pointerKey : fieldNodesToPrivatize) {
        fieldsToPrivatize.add(pointerKey.getField());
    }
  }

  private void markLCDAccesses() {
    for (ConcurrentFieldAccess access : accesses) {
      if (!isLCD(access)) {
        accessesNotLCD.add(access);
      } else {
        accessesLCD.add(access);
      }
    }
  }

  private void gatherAllMustNotPrivatizeFields() {
    for (ConcurrentFieldAccess access : accessesLCD) {
      for (FieldAccess fieldAccess : access.betaAccesses) {
        mustNotPrivatize.add(fieldAccess.f);
      }
    }
  }

  private void gatherAllPrivatizableHeapNodes() {
    for (ConcurrentFieldAccess access : accessesNotLCD) {
      for (FieldAccess fieldAccess : access.betaAccesses) {
        AccessTrace accessTrace = new AccessTrace(a, fieldAccess.n, fieldAccess.getRef());
        accessTrace.compute();
        instancesToPrivatize.addAll(accessTrace.getinstances());
        fieldNodesToPrivatize.addAll(accessTrace.getPointers());
      }
    }
  }

  /**
   * we check that for this ConccurrentFieldAcceess, \forall r \exists w .
   * happensBefore(w,r) \land !happensBefore(r,w)
   * 
   * @param access
   * @return
   */
  private boolean isLCD(ConcurrentFieldAccess access) {
    LinkedHashSet<FieldAccess> betaAccesses = access.betaAccesses;
    for (FieldAccess readFieldAccess : betaAccesses)
      if (readFieldAccess instanceof ReadFieldAccess) {
        boolean ok = false;
        for (FieldAccess writeFieldAccess : betaAccesses) {
          if (writeFieldAccess instanceof WriteFieldAccess) {
            StatementOrder writeBeforeRead = new StatementOrder(a.callGraph, writeFieldAccess.n, writeFieldAccess.i,
                readFieldAccess.n, readFieldAccess.i);
            StatementOrder readBeforeWrite = new StatementOrder(a.callGraph, writeFieldAccess.n, writeFieldAccess.i,
                readFieldAccess.n, readFieldAccess.i);
            if (writeBeforeRead.happensBefore() && !readBeforeWrite.happensBefore()) {
              ok = true;
              break;
            }
          }
        }
        if (!ok)
          return false;
      }
    return true;
  }
}
