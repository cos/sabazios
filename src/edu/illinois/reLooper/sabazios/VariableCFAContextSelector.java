/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.illinois.reLooper.sabazios;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallString;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallStringContextSelector;

import extra166y.ParallelArray;

public class VariableCFAContextSelector extends CallStringContextSelector {

	public VariableCFAContextSelector(ContextSelector base) {
		super(base);
	}
	
	private final int HIGH_CFA = 10;
	private final int LOW_CFA = 0;

	@Override
	protected int getLength(CGNode caller, CallSiteReference site,
			IMethod target) {
		String string = site.getDeclaredTarget().toString()+caller.getMethod().toString();
		
		if(string.contains(ParallelArray.OP_STRING))
			return HIGH_CFA;
		
		CallString callString = (CallString) caller.getContext().get(
				CALL_STRING);
		if (callString != null) {
			IMethod[] methods = callString.getMethods();
			for (IMethod iMethod : methods) {
				if(iMethod.toString().contains(ParallelArray.OP_STRING))
					return HIGH_CFA;
			}
		}
		return LOW_CFA;
	}
}
