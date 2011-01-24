package edu.illinois.reLooper.sabazios;

import com.ibm.wala.demandpa.alg.refinepolicy.AbstractRefinementPolicy;
import com.ibm.wala.demandpa.alg.refinepolicy.AlwaysRefineCGPolicy;
import com.ibm.wala.demandpa.alg.refinepolicy.AlwaysRefineFieldsPolicy;
import com.ibm.wala.demandpa.alg.refinepolicy.RefinementPolicy;
import com.ibm.wala.demandpa.alg.refinepolicy.RefinementPolicyFactory;

public final class MyRefinementPolicy extends AbstractRefinementPolicy {
	public static final class Factory implements
			RefinementPolicyFactory {
		@Override
		public RefinementPolicy make() {
			return new MyRefinementPolicy();
		}
	}

	public MyRefinementPolicy() {
		super(new AlwaysRefineFieldsPolicy(), new AlwaysRefineCGPolicy());
	}
}