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
		
		System.out.println("COMPARE "+this+" <<<<>>>> "+otherF);
		System.out.println("COMPARE on "+key+": "+this.getItem(key)+" <<<<>>>> "+otherF.getItem(key));
		
		return this.getItem(key).equals(otherF.getItem(key));
	}
}