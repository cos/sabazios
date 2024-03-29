package racefix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import racefix.refactoring.ClassChangeSet;
import racefix.refactoring.RefactoringEngine;
import sabazios.A;
import sabazios.domains.ConcurrentFieldAccess;
import sabazios.domains.FieldAccess;
import sabazios.domains.ReadFieldAccess;
import sabazios.domains.WriteFieldAccess;
import sabazios.util.FlexibleContext;
import sabazios.wala.CS;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;

public class Privatizer {

	private final A a;
	private final Set<ConcurrentFieldAccess> accesses;

	private final Set<ConcurrentFieldAccess> accessesNotLCD = new LinkedHashSet<ConcurrentFieldAccess>();
	private final Set<ConcurrentFieldAccess> accessesLCD = new LinkedHashSet<ConcurrentFieldAccess>();

	private final Set<PointerKey> fieldNodesToPrivatize = new LinkedHashSet<PointerKey>();
	private final Set<InstanceKey> instancesToPrivatize = new LinkedHashSet<InstanceKey>();

	private final Set<IField> fieldsToPrivatize = new LinkedHashSet<IField>();

	private final Set<IField> mustNotPrivatize = new LinkedHashSet<IField>();

	private final Set<IField> starredFields = new HashSet<IField>();
	private IClass classWithComputation;
	private List<String> okInstances = new ArrayList<String>();

	public Privatizer(A a, Set<ConcurrentFieldAccess> accesses) {
		this.a = a;
		this.accesses = accesses;
	}

	public Set<Object> compute() {
		getSafeInstances();
		markLCDAccesses();
		gatherAllPrivatizableHeapNodes();
		gatherAllMustNotPrivatizeFields();
		gatherPrivatizableFields();
		markStarredFields();
		markClassWithComputation();
		return null;
	}

	private void getSafeInstances() {
		File file = new File("validInstances.txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
		}

		try {
			while (reader.ready())
				okInstances.add(reader.readLine());
		} catch (IOException e) {
		}
	}

	public void refactor() {
		ClassChangeSet changeSet = new ClassChangeSet();
		changeSet.threadLocal = new LinkedHashSet<String>();
		for (PointerKey pointerKey : fieldNodesToPrivatize) {
			if (pointerKey instanceof InstanceFieldKey) {
				InstanceFieldKey instanceFieldKey = (InstanceFieldKey) pointerKey;
				String packageName = instanceFieldKey.getField()
						.getDeclaringClass().getName().getPackage().toString();
				String className = instanceFieldKey.getField()
						.getDeclaringClass().getName().getClassName()
						.toString();
				String fieldName = instanceFieldKey.getField().getName()
						.toString();
				String qualifiedFieldName = "src." + packageName + "."
						+ className + "." + fieldName;

				// TODO something more clever than this
				if (qualifiedFieldName.contains("this"))
					continue;

				changeSet.threadLocal.add(qualifiedFieldName);
			}
		}

		RefactoringEngine engine = new RefactoringEngine(changeSet);
		engine.applyRefactorings();
	}

	private void markClassWithComputation() {
		CGNode someNodeInsideTheLoop = accesses.iterator().next().alphaAccesses
				.iterator().next().n;
		FlexibleContext context = (FlexibleContext) someNodeInsideTheLoop
				.getContext();
		CGNode theMethodThatCallsTheParallelComputation = (CGNode) context
				.getItem(CS.OPERATOR_CALLER);
		classWithComputation = theMethodThatCallsTheParallelComputation
				.getMethod().getDeclaringClass();
	}

	public boolean shouldBeThreadLocal(IField f) {
		return f.isStatic()
				|| f.getDeclaringClass().equals(classWithComputation);
	}

	private void markStarredFields() {
		for (InstanceKey instanceKey : this.instancesToPrivatize) {
			Iterator<Object> predNodes = a.heapGraph.getPredNodes(instanceKey);
			Set<IField> fieldsSeenSoFar = new HashSet<IField>();
			while (predNodes.hasNext()) {
				Object object = (Object) predNodes.next();
				if (object instanceof InstanceFieldKey) {
					InstanceFieldKey f = (InstanceFieldKey) object;
					IField iField = f.getField();
					if (fieldsSeenSoFar.contains(iField)) {
						starredFields.add(iField);
					} else
						fieldsSeenSoFar.add(iField);
				}
			}
		}
	}

	private void gatherPrivatizableFields() {
		for (PointerKey pointerKey : fieldNodesToPrivatize) {
			if (pointerKey instanceof InstanceFieldKey) {
				InstanceFieldKey instanceFieldKey = (InstanceFieldKey) pointerKey;
				fieldsToPrivatize.add(instanceFieldKey.getField());
			}
		}
	}

	private void markLCDAccesses() {
		for (ConcurrentFieldAccess access : accesses) {

			if (isOK(access)) {
				if (!isLCD(access)) {
					accessesNotLCD.add(access);
				} else {
					accessesLCD.add(access);
				}
			} else {
				accessesNotLCD.add(access);
			}
		}

	}

	private boolean isOK(ConcurrentFieldAccess access) {
		for (String s : okInstances) {
			if (access.toString().contains(s))
				return true;
		}
		return false;
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
				System.out.println(fieldAccess.toString());
				AccessTrace accessTrace = new AccessTrace(a, fieldAccess.n,
						fieldAccess.getRef());
				accessTrace.compute();
				instancesToPrivatize.addAll(accessTrace.getinstances());
				fieldNodesToPrivatize.addAll(accessTrace.getPointers());
			}
		}
	}

	private boolean isLCD(ConcurrentFieldAccess access) {
		return !noLCD(access);
	}

	/**
	 * we check that for this ConccurrentFieldAcceess, \forall r \exists w .
	 * happensBefore(w,r) \land !happensBefore(r,w)
	 * 
	 * @param access
	 * @return
	 */
	private boolean noLCD(ConcurrentFieldAccess access) {
		LinkedHashSet<FieldAccess> betaAccesses = access.betaAccesses;
		for (FieldAccess readFieldAccess : betaAccesses)
			if (readFieldAccess instanceof ReadFieldAccess) {
				boolean ok = false;
				for (FieldAccess writeFieldAccess : betaAccesses) {
					if (writeFieldAccess instanceof WriteFieldAccess) {
						StatementOrder writeBeforeRead = new StatementOrder(
								a.callGraph, writeFieldAccess.n,
								writeFieldAccess.i, readFieldAccess.n,
								readFieldAccess.i);
						StatementOrder readBeforeWrite = new StatementOrder(
								a.callGraph, readFieldAccess.n,
								readFieldAccess.i, writeFieldAccess.n,
								writeFieldAccess.i);
						boolean wBr = writeBeforeRead.happensBefore();
						boolean rBw = readBeforeWrite.happensBefore();
						if (wBr && !rBw) {
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

	String getAccessesInLCDTestString() {
		String s = "";
		for (ConcurrentFieldAccess access : accessesLCD) {
			s += access.toString() + "\n";
		}
		return s;
	}

	String getAccessesNotInLCDTestString() {
		String s = "";
		for (ConcurrentFieldAccess access : accessesNotLCD) {
			s += access.toString() + "\n";
		}
		return s;
	}

	String getStarredFields() {
		return starredFields.toString();
	}

	public Set<PointerKey> getFieldNodesToPrivatize() {
		return fieldNodesToPrivatize;
	}

	public Set<InstanceKey> getInstancesToPrivatize() {
		return instancesToPrivatize;
	}

}
