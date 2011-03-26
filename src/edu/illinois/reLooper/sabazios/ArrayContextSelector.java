package edu.illinois.reLooper.sabazios;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;

final class ArrayContextSelector implements ContextSelector {

	public static final ContextKey NODE = new FlexibleContext.NamedContextKey("NODE");
	public static final ContextKey ELEMENT_VALUE = new FlexibleContext.NamedContextKey("ELEMENT_VALUE");
	public static final ContextKey MAIN_ITERATION = new FlexibleContext.NamedContextKey("MAIN_ITERATION");
	public static final ContextKey PARALLEL = new FlexibleContext.NamedContextKey("PARALLEL");
	public static final ContextKey ARRAY = new FlexibleContext.NamedContextKey("ARRAY");
	public static final ContextKey CALL_SITE_REFERENCE = new FlexibleContext.NamedContextKey("CALL_SITE_REFERENCE");

	ZeroXInstanceKeys keyFactory;
	
	public ArrayContextSelector(ZeroXInstanceKeys keyFactory) {
		this.keyFactory = keyFactory;
	}

	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {

		String calleeString = callee.toString();
		if (calleeString.contains("replaceWithGeneratedValue") || calleeString.contains("apply")
				|| calleeString.contains("replaceWithMappedIndex")) {
			FlexibleContext c = new FlexibleContext(caller.getContext());
			c.putItem(ARRAY, receiver);
			System.out.println(c);
			return c;
		}

		String callerMethod = caller.getMethod().toString();
		if (callerMethod.contains("replaceWithGeneratedValueSeq") && calleeString.contains("op")) {
			FlexibleContext c = new FlexibleContext(caller.getContext());
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getDef());
			c.putItem(MAIN_ITERATION, invoke.getDef() == 5);
			c.putItem(PARALLEL, false);
			System.out.println(c);
			return c;
		}

		if (callerMethod.contains("replaceWithGeneratedValue") && calleeString.contains("op")) {
			FlexibleContext c = new FlexibleContext(caller.getContext());
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getDef());
			c.putItem(MAIN_ITERATION, invoke.getDef() == 5);
			c.putItem(PARALLEL, true);
			c.putItem(CALL_SITE_REFERENCE, site);
			System.out.println(c);
			return c;
		}

		if (callerMethod.contains("replaceWithMappedIndexSeq") && calleeString.contains("op")) {
			FlexibleContext c = new FlexibleContext(caller.getContext());
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getDef());
			c.putItem(MAIN_ITERATION, invoke.getDef() == 6);
			c.putItem(PARALLEL, false);
			c.putItem(CALL_SITE_REFERENCE, site);
			System.out.println(c);
			return c;
		}
		
		if (callerMethod.contains("replaceWithMappedIndex") && calleeString.contains("op")) {
			FlexibleContext c = new FlexibleContext(caller.getContext());
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getDef());
			c.putItem(MAIN_ITERATION, invoke.getDef() == 6);
			c.putItem(PARALLEL, true);
			c.putItem(CALL_SITE_REFERENCE, site);
			System.out.println(c);
			return c;
		}

		if (callerMethod.contains("applySeq") && calleeString.contains("op")) {
			FlexibleContext c = new FlexibleContext(caller.getContext());
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getUse(1));
			c.putItem(MAIN_ITERATION, invoke.getUse(1) == 4);
			c.putItem(PARALLEL, false);
			System.out.println(c);
			return c;
		}
		if (callerMethod.contains("apply") && calleeString.contains("op")) {
			FlexibleContext c = new FlexibleContext(caller.getContext());
			c.putItem(NODE, caller);
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			c.putItem(ELEMENT_VALUE, invoke.getUse(1));
			c.putItem(MAIN_ITERATION, invoke.getUse(1) == 4);
			c.putItem(PARALLEL, true);
			System.out.println(c);
			return c;
		}
		
		if(!keyFactory.isInteresting(callee.getDeclaringClass()))
			return Everywhere.EVERYWHERE;

		return caller.getContext();
	}
}