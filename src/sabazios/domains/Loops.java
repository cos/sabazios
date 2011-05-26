package sabazios.domains;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class Loops implements Iterable<Loop> {
	HashSet<Loop> all = new HashSet<Loop>();
	
	public Loop get(InstanceKey array, CGNode operatorCaller, CallSiteReference operatorCallSite) {
		for (Loop t: all) {
			if(t.array.equals(array) && t.operatorCaller.equals(operatorCaller) && t.operatorCallSite.equals(operatorCallSite))
				return t;
		}
		Loop t = new Loop(array, operatorCaller, operatorCallSite);
		all.add(t);
		return t;
	}

	@Override
	public Iterator<Loop> iterator() {
		return all.iterator();
	}
}