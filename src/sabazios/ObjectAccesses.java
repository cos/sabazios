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


public abstract class ObjectAccesses<T extends FieldAccess> extends HashMap<Loop, HashMap<InstanceKey, HashSet<T>>> {
	private static final long serialVersionUID = -5571882875222007305L;

	protected abstract class ObjectAccessesGatherer extends InstructionsGatherer {
		public ObjectAccessesGatherer(RaceAnalysis a) {
			super(a);
		}

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

	}
	private RaceAnalysis a;
	protected ObjectAccessesGatherer oag;

	public ObjectAccesses(RaceAnalysis a) {
		this.a = a;
	}

	@Override
	public String toString() {
		return toString("");
	}
	
	public String toString(String linePrefix) {
		StringBuffer s = new StringBuffer();
		for (Loop t : this.keySet()) {
			
			s.append(linePrefix + "Thread: "+t);
			
			HashMap<InstanceKey, HashSet<T>> thisWithinThread = this.get(t);
			for (InstanceKey o : thisWithinThread.keySet()) {
				s.append("\n"+linePrefix+"    Object: ");
				s.append(U.tos(o));
				for (T w : thisWithinThread.get(o)) {
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

	protected void add(T w) {
		FlexibleContext c = (FlexibleContext) w.n.getContext();
		Loop t = a.t.get((InstanceKey) c.getItem(CS.ARRAY),
				(CGNode) c.getItem(CS.OPERATOR_CALLER), (CallSiteReference) c.getItem(CS.OPERATOR_CALL_SITE_REFERENCE));
		
		HashMap<InstanceKey, HashSet<T>> localAcccess;
		if(this.containsKey(t)) 			
			localAcccess = this.get(t);
		else {
			localAcccess = new HashMap<InstanceKey, HashSet<T>>();
			this.put(t, localAcccess);
		}
		

		if (!localAcccess.containsKey(w.o))
			localAcccess.put(w.o, new HashSet<T>());
		HashSet<T> set = localAcccess.get(w.o);
		set.add(w);
	}
	
	public int size() {
		int n = 0;
		for(HashMap<InstanceKey, HashSet<T>> x : this.values()) {
			for(HashSet<T> y : x.values()) {
				n += y.size();
			}
		}
		return n;
	}

	protected abstract boolean rightIteration(FlexibleContext c);

	public void compute() {
		this.oag.compute();		
	}
}
