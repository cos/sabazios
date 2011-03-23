package edu.illinois.reLooper.sabazios;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ArrayOperatorContext implements Context {

	public static final ContextKey PARALLEL_OPERATOR = new ContextKey() {
	};
	public static final ContextKey ELEMENT = new ContextKey() {
	};

	class IsParallel implements ContextItem {
		boolean parallel;
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof IsParallel))
				return false;
			return parallel == ((IsParallel)obj).parallel;
		}
	}

	public static class Element implements ContextItem {
		int element;
		CGNode cgNode;
		public boolean equals(Object obj) {
			if(!(obj instanceof Element))
				return false;
			return cgNode.equals( ((Element)obj).cgNode) && element == ((Element)obj).element;
		}
		
		@Override
		public int hashCode() {
			return element * cgNode.hashCode() * 7841;
		}
	}

	IsParallel isParallel = new IsParallel();
	private Element element = new Element();

	public ArrayOperatorContext(CGNode cgNode, int i, boolean isParallel) {
		this.element.cgNode = cgNode;
		this.isParallel.parallel = isParallel;
		this.element.element = i;
	}

	@Override
	public ContextItem get(ContextKey name) {
		if (name == PARALLEL_OPERATOR)
			return isParallel;
		if (name == ELEMENT)
			return element;
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ArrayOperatorContext))
			return false;
		ArrayOperatorContext other = ((ArrayOperatorContext)obj);
		return element.equals(other.element) && this.isParallel == other.isParallel;
	}
	
	@Override
	public int hashCode() {
		return 7841 * element.hashCode() * (isParallel.parallel ? 1 : 2);
	}
}
