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


	public ArrayContextSelector() {
	}

	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		if(caller.toString().contains("replaceWithGeneratedValue") && callee.toString().contains("op"))
			return new ArrayOperatorContext( caller, -1, true);
		if(caller.toString().contains("apply") && callee.toString().contains("op"))
		{
			SSAAbstractInvokeInstruction invoke = caller.getIR().getCalls(site)[0];
			return new ArrayOperatorContext( caller, invoke.getUse(1), true);
		}
		
		return caller.getContext();
	}
}