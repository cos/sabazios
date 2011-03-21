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

	public static final class UniqueContext implements Context {

		private int hashCode;

		public UniqueContext() {
			this.hashCode = (int) (Math.random() * 10000);
		}

		@Override
		public int hashCode() {
			return hashCode;
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

	public final static UniqueContext PAROP_CONTEXT = new UniqueContext();
	public final static UniqueContext SEQOP_CONTEXT = new UniqueContext();

	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		String string = site.getDeclaredTarget().toString();
		if (opSelector.isParOp(caller, site, callee, receiver)) {
			System.err.println("Found par operator string.");
			return PAROP_CONTEXT;
		} else if (opSelector.isSeqOp(caller, site, callee, receiver)){
			System.err.println("Found seq operator string.");
			return SEQOP_CONTEXT;
		}
		else
			return caller.getContext();
	}
}