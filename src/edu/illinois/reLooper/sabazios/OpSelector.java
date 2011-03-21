package edu.illinois.reLooper.sabazios;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public interface OpSelector {
	public boolean isParOp(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver);
	public boolean isSeqOp(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver);
}
