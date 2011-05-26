package sabazios.util;

import java.util.Set;

import sabazios.wala.CS;

import com.ibm.wala.ipa.callgraph.AbstractCallContext;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class FlexibleContext extends AbstractCallContext {
	private Context inner;

	public FlexibleContext() {
	}
	
	public FlexibleContext(Context inner) {
		this.inner = inner;
	}
	
	@Override
	public ContextItem get(ContextKey k) {
		ContextItem o = super.get(k);
		if(o != null)
			return o;
		else
			if(inner != null)
				return inner.get(k);
		return null;
	}

	public void putItem(ContextKey k, Object o) {
		this.put(k, new FlexibleContextItem(o));
	}
	
	public Object getItem(ContextKey k) {
		FlexibleContextItem item = (FlexibleContextItem) this.get(k);
		if(item != null)
			return item.o;
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
		
		return super.equals(other) && (inner != null ? inner.equals(other.inner) : other.inner == null);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + (inner != null ? inner.hashCode() : 0);
	}
	
	@Override
	public String toString() {
		String s = "";
		Set<ContextKey> keySet = this.keySet();
		for (ContextKey contextKey : keySet) {
			Object object = this.getItem(contextKey);
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
		Set<ContextKey> keySet = this.keySet();
		for (ContextKey contextKey : keySet) {
			Object object = this.getItem(contextKey);
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
			if(obj == null)
				return false;
			if(obj.getClass() != this.getClass())
				return false;
			
			FlexibleContextItem other = (FlexibleContextItem) obj;
			return o.equals(other.o);
		}
		@Override
		public int hashCode() {
			if(o == null)
				return 0;
			else
				return o.hashCode();
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