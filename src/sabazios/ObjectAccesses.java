package sabazios;

import java.util.HashMap;
import java.util.HashSet;

import sabazios.domains.FieldAccess;
import sabazios.domains.Loop;
import sabazios.util.FlexibleContext;
import sabazios.util.InstructionsGatherer;
import sabazios.util.U;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;


public abstract class ObjectAccesses<T extends FieldAccess> extends InstructionsGatherer {
	public ObjectAccesses(RaceAnalysis a) {
		super(a);
	}

	HashMap<Loop, HashMap<InstanceKey, HashSet<T>>> accesses = new HashMap<Loop, HashMap<InstanceKey, HashSet<T>>>();

	@Override
	public String toString() {
		return toString("");
	}
	
	public String toString(String linePrefix) {
		StringBuffer s = new StringBuffer();
		for (Loop t : accesses.keySet()) {
			
			s.append(linePrefix + "Thread: "+t);
			
			HashMap<InstanceKey, HashSet<T>> accessesWithinThread = accesses.get(t);
			for (InstanceKey o : accessesWithinThread.keySet()) {
				s.append("\n"+linePrefix+"    Object: ");
				s.append(U.tos(o));
				for (T w : accessesWithinThread.get(o)) {
					s.append("\n"+linePrefix+"       - ");
					s.append(w.toString());
				}
			}
		}
		return s.toString();
	}

	String[] threadSafeMethods = new String[] { 
			"java/util/regex/Pattern", "java/lang/System, exit",
			"java/io/PrintStream, print", "java/lang/Throwable, printStackTrace",
			"java/security/AccessControlContext, getDebug", "java.io.PrintStream, format", "java/util/Random, <init>" , "Integer, <init>",
			};

	@Override
	protected boolean shouldVisit(CGNode n) {
		for (String pattern : threadSafeMethods)
			if (n.getMethod().toString().contains(pattern))
				return false;
		return true;
	}

	@Override
	protected boolean shouldAnalyze(CGNode n) {
		if (!(n.getContext() instanceof FlexibleContext))
			return false;

		FlexibleContext c = (FlexibleContext) n.getContext();
		return c.getItem(CS.PARALLEL) != null && ((Boolean) c.getItem(CS.PARALLEL)) && rightIteration(c)
				&& n.getIR() != null;
	}

	protected void add(T w) {
		FlexibleContext c = (FlexibleContext) w.n.getContext();
		Loop t = a.t.get((InstanceKey) c.getItem(CS.ARRAY),
				(CGNode) c.getItem(CS.OPERATOR_CALLER), (CallSiteReference) c.getItem(CS.OPERATOR_CALL_SITE_REFERENCE));
		
		HashMap<InstanceKey, HashSet<T>> localAcccess;
		if(accesses.containsKey(t)) 			
			localAcccess = this.accesses.get(t);
		else {
			localAcccess = new HashMap<InstanceKey, HashSet<T>>();
			accesses.put(t, localAcccess);
		}
		

		if (!localAcccess.containsKey(w.o))
			localAcccess.put(w.o, new HashSet<T>());
		HashSet<T> set = localAcccess.get(w.o);
		set.add(w);
	}
	
	public int size() {
		int n = 0;
		for(HashMap<InstanceKey, HashSet<T>> x : this.accesses.values()) {
			for(HashSet<T> y : x.values()) {
				n += y.size();
			}
		}
		return n;
	}

	protected abstract boolean rightIteration(FlexibleContext c);
}
