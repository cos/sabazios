package edu.illinois.reLooper.sabazios;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;

import extra166y.ParallelArray;

final class ArrayContextSelector implements ContextSelector {

	public static final ContextKey NODE = new FlexibleContext.NamedContextKey("NODE");
	public static final ContextKey ELEMENT_VALUE = new FlexibleContext.NamedContextKey("ELEMENT_VALUE");
	public static final ContextKey PARALLEL = new FlexibleContext.NamedContextKey("PARALLEL");

	public ArrayContextSelector() {
	}

	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		if (caller.toString().contains("replaceWithGeneratedValueSeq") && callee.toString().contains("op")) {
			FlexibleContext c = new FlexibleContext();
			c.putItem(NODE, caller);
			c.putItem(ELEMENT_VALUE, -1);
			c.putItem(PARALLEL, false);
			return c;
		}

		if (caller.toString().contains("replaceWithGeneratedValue") && callee.toString().contains("op")) {
			FlexibleContext c = new FlexibleContext();
			c.putItem(NODE, caller);
			c.putItem(ELEMENT_VALUE, -1);
			c.putItem(PARALLEL, true);
			return c;
		}

		if (caller.toString().contains("applySeq") && callee.toString().contains("op")) {
			FlexibleContext c = new FlexibleContext();
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getUse(1));
			c.putItem(PARALLEL, false);
			return c;
		}
		if (caller.toString().contains("apply") && callee.toString().contains("op")) {
			FlexibleContext c = new FlexibleContext();
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getUse(1));
			c.putItem(PARALLEL, true);
			return c;
		}

		return caller.getContext();
	}
}