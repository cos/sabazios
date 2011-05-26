package sabazios.util;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.ibm.wala.ipa.callgraph.CallContext;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;

public class ContextUtil {

	public static boolean equalsExecept(CallContext c1, CallContext c2, Set<ContextKey> keys) {
		Map<ContextKey, ValueDifference<ContextItem>> entriesDiffering = Maps.difference(c1, c2).entriesDiffering();
		Set<ContextKey> differentEntries = entriesDiffering.keySet();	
		return differentEntries.equals(keys);
	}
	
}
