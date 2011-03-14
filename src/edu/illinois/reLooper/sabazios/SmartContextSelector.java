package edu.illinois.reLooper.sabazios;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

import extra166y.ParallelArray;

final class SmartContextSelector implements ContextSelector {
	
	private final OpSelector opSelector;

	public SmartContextSelector(OpSelector opSelector) {
		this.opSelector = opSelector;
	}
	
	private static final ContextKey INSIDE_PAR_OP = new ContextKey() {
	};

	public static final class InsideParOpContext implements Context {

		/**
		 * Don't use default hashCode (java.lang.Object) as it's
		 * nondeterministic.
		 */
		@Override
		public int hashCode() {
			return 345134;
		}

		@Override
		public ContextItem get(ContextKey name) {
			return null;
		}
		
		@Override
		public String toString() {
			return "Inside par-op";
		}
	}
	
	private final static InsideParOpContext INSIDE_CONTEXT = new InsideParOpContext(); 

	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		String string = site.getDeclaredTarget().toString();
		if(opSelector.accepts(caller, site, callee, receiver))
		{
			System.err.println("Found the operator string.");
			return INSIDE_CONTEXT;
		}
		else
			return caller.getContext();
	}
}