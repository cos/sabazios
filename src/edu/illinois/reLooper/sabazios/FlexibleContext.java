package edu.illinois.reLooper.sabazios;

import java.util.HashMap;
import java.util.HashSet;

import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;

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
		return items.get(k);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof FlexibleContext))
			return false;
		if(obj == null)
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
		return items.toString() + " | " + inner;
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
			return s.toString().equals(obj.toString());
		}
	}
}