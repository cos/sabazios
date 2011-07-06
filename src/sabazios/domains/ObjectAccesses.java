package sabazios.domains;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import sabazios.A;
import sabazios.util.FlexibleContext;
import sabazios.util.InstructionsGatherer;
import sabazios.util.U;
import sabazios.wala.CS;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;


public abstract class ObjectAccesses<T extends FieldAccess> extends LinkedHashMap<Loop, Map<InstanceKey, Set<T>>> {
	private static final long serialVersionUID = -5571882875222007305L;

	protected abstract class ObjectAccessesGatherer extends InstructionsGatherer {

		@Override
		protected boolean shouldVisit(CGNode n) {
			return !CS.threadSafe(n);
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
	protected ObjectAccessesGatherer oag;
	private final A a;

	public ObjectAccesses(A a) {
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
			
			Map<InstanceKey, Set<T>> thisWithinThread = this.get(t);
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

	protected void add(T w) {
		FlexibleContext c = (FlexibleContext) w.n.getContext();
		Loop t = a.loops.get((InstanceKey) c.getItem(CS.ARRAY),
				(CGNode) c.getItem(CS.OPERATOR_CALLER), (CallSiteReference) c.getItem(CS.OPERATOR_CALL_SITE_REFERENCE));
		
		Map<InstanceKey, Set<T>> localAcccess;
		if(this.containsKey(t)) 			
			localAcccess = this.get(t);
		else {
			localAcccess = new LinkedHashMap<InstanceKey, Set<T>>();
			this.put(t, localAcccess);
		}
		

		if (!localAcccess.containsKey(w.o))
			localAcccess.put(w.o, new HashSet<T>());
		Set<T> set = localAcccess.get(w.o);
		set.add(w);
	}
	
	public int size() {
		int n = 0;
		for(Map<InstanceKey, Set<T>> x : this.values()) {
			for(Set<T> y : x.values()) {
				n += y.size();
			}
		}
		return n;
	}

	protected abstract boolean rightIteration(FlexibleContext c);

	public void compute(A a) {
		this.oag.compute(a);		
	}
}
