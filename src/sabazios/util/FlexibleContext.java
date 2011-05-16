package sabazios.util;

import java.util.HashMap;
import java.util.Set;

import sabazios.CS;

import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class FlexibleContext implements Context {
	HashMap<ContextKey, Object> items = new HashMap<ContextKey, Object>();
	private Context inner;

	public FlexibleContext() {
	}
	
	public FlexibleContext(Context inner) {
		this.inner = inner;
	}
	
	@Override
	public ContextItem get(ContextKey k) {
		Object o = items.get(k);
		if(o != null)
			return new FlexibleContextItem(o);
		else
			if(inner != null)
				return inner.get(k);
		return null;
	}

	public void putItem(ContextKey k, Object o) {
		items.put(k, o);
	}
	
	public Object getItem(ContextKey k) {
		Object item = items.get(k);
		if(item != null)
			return item;
		else
			if(inner instanceof FlexibleContext)
				return ((FlexibleContext) inner).getItem(k);
		return null;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof FlexibleContext))
			return false;
		
		FlexibleContext other = (FlexibleContext) obj;
		
		return this.items.equals(other.items) && (inner != null ? inner.equals(other.inner) : other.inner == null);
	}
	
	@Override
	public int hashCode() {
		return items.hashCode() + (inner != null ? inner.hashCode() : 0);
	}
	
	@Override
	public String toString() {
		String s = "";
		Set<ContextKey> keySet = items.keySet();
		for (ContextKey contextKey : keySet) {
			Object object = items.get(contextKey);
			if(object instanceof InstanceKey) {
				InstanceKey i = (InstanceKey) object;
				s+= "["+contextKey + " => " + U.tos(i) + "]";
			} else if(contextKey == CS.MAIN_ITERATION) 
				s += ((Boolean)object) ? "[m]" : "[c]";
			else if(contextKey == CS.PARALLEL) 
				s += ((Boolean)object) ? "[P]" : "[S]";
			else
				s += "["+contextKey + " => "+object.toString()+"]"; 
		}
		return s + inner;
	}
	
	public String toDotString() {
		String s = "";
		Set<ContextKey> keySet = items.keySet();
		for (ContextKey contextKey : keySet) {
			Object object = items.get(contextKey);
			if(object instanceof InstanceKey) {
				InstanceKey i = (InstanceKey) object;
				s+= "["+contextKey + " => " + U.tos(i) + "]\\n";
			} else if(contextKey == CS.MAIN_ITERATION) 
				s += ((Boolean)object) ? "[main]\\n" : "[check]\\n";
			else if(contextKey == CS.PARALLEL) 
				s += ((Boolean)object) ? "[par]" : "[seq]\\n";
			else
				s += "["+contextKey + " => "+object.toString()+"]\\n"; 
		}
		if(inner instanceof FlexibleContext)
			return s + ((FlexibleContext) inner).toDotString();
		else
			return s + inner;
	}
	
	public static class FlexibleContextItem implements ContextItem {
		private Object o;
		public FlexibleContextItem(Object o) {
			this.o = o;
		}
		@Override
		public boolean equals(Object obj) {
			return o.equals(obj);
		}
	}
	
	public static class NamedContextKey implements ContextKey {
		private String s;
		
		public NamedContextKey(String s) {
			this.s = s;
		}
		
		@Override
		public String toString() {
			return s;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null)
				return false;
			return s.equals(obj.toString());
		}
	}

	/**
	 * Are the two contexts equal with respect to key?
	 * @param other
	 * @param key
	 * @return
	 */
	public boolean equals(Context other, ContextKey key) {
		if(!(other instanceof FlexibleContext))
			return false;
		
		FlexibleContext otherF = (FlexibleContext) other;
		if(this.getItem(key)==null)
			return otherF.getItem(key) == null;
		
		return this.getItem(key).equals(otherF.getItem(key));
	}
}