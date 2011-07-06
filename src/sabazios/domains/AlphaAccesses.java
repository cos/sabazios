package sabazios.domains;

import java.util.Iterator;

import sabazios.A;
import sabazios.util.ArrayContents;
import sabazios.util.FlexibleContext;
import sabazios.util.U;
import sabazios.wala.CS;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

public class AlphaAccesses extends ObjectAccesses<WriteFieldAccess> {
	private static final long serialVersionUID = 3482224366938834900L;

	public AlphaAccesses(final A a) {
		super(a);
		oag = new ObjectAccessesGatherer() {

			@Override
			protected void visit(CGNode n, SSAInstruction i) {
				if (i instanceof SSAPutInstruction) {

					SSAPutInstruction pi = (SSAPutInstruction) i;
					IField f = a.cha.resolveField(pi.getDeclaredField());
					if (!pi.isStatic()) {
						LocalPointerKey pk = a.pointerForValue.get(n, pi.getRef());
						Iterator<Object> succNodes = a.heapGraph.getSuccNodes(pk);
						while (succNodes.hasNext()) {
							InstanceKey o = (InstanceKey) succNodes.next();
							// if (!U.isMainContext(o)) {
							WriteFieldAccess w = new WriteFieldAccess(a, n, pi, o, f);
							add(w);
							// }
						}
					} else {
						WriteFieldAccess w = new WriteFieldAccess(a, n, pi, null, f);
						add(w);
					}
				}
				if (i instanceof SSAArrayStoreInstruction) {
					SSAArrayStoreInstruction asi = (SSAArrayStoreInstruction) i;
					LocalPointerKey pk = a.pointerForValue.get(n, asi.getArrayRef());
					Iterator<Object> succNodes = a.heapGraph.getSuccNodes(pk);
					IField f = ArrayContents.v();
					while (succNodes.hasNext()) {
						InstanceKey o = (InstanceKey) succNodes.next();
						if (!U.isAlphaIteration(o)) {
							WriteFieldAccess w = new WriteFieldAccess(a, n, asi, o, f);
							add(w);
						}
					}
				}
			}
		};
	}

	@Override
	protected boolean rightIteration(FlexibleContext c) {
		return ((Boolean) c.getItem(CS.MAIN_ITERATION));
	}
}
