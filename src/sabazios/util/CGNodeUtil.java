package sabazios.util;

import com.google.common.collect.Sets;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallContext;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;

public abstract class CGNodeUtil {
	public static boolean equals(CGNode n1, CGNode n2, ContextKey key) {
		ContextItem contextItem1 = n1.getContext().get(key);
		ContextItem contextItem2 = n2.getContext().get(key);
		if (contextItem1 == null)
			return contextItem2 == null;
		else
			return contextItem1.equals(contextItem2);
	}

	public static boolean equals(CGNode n1, CGNode n2, ContextKey... keys) {
		for (ContextKey key : keys) {
			if (!CGNodeUtil.equals(n1, n2, key))
				return false;
		}
		return true;
	}
	public static boolean equalsExcept(CGNode n1, CGNode n2, ContextKey... keys) {
		if(!n1.getMethod().equals(n2.getMethod()))
			return false;
		if(!(n1.getContext() instanceof CallContext) || !(n2.getContext() instanceof CallContext))
			return true;
		
		CallContext c1 = (CallContext) n1.getContext();
		CallContext c2 = (CallContext) n2.getContext();
		return ContextUtil.equalsExecept(c1, c2, Sets.newHashSet(keys));
	}
}
