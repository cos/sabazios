package sabazios;

import java.util.Iterator;

import sabazios.domains.FieldAccess;
import sabazios.domains.ReadFieldAccess;
import sabazios.domains.WriteFieldAccess;
import sabazios.util.ArrayContents;
import sabazios.util.FlexibleContext;
import sabazios.util.U;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;


public class OtherAccesses extends ObjectAccesses<FieldAccess> {
	private static final long serialVersionUID = 6340026016860262601L;

	public OtherAccesses() {
		oag = new ObjectAccessesGatherer() {
			@Override
			protected void visit(CGNode n, SSAInstruction i) {
				if (i instanceof SSAGetInstruction) {
					SSAGetInstruction pi = (SSAGetInstruction) i;
					IField f = A.cha.resolveField(pi.getDeclaredField());
					if(f == null)
						return;
					if (!pi.isStatic()) {
						LocalPointerKey pk = A.pointerForValue.get(n, pi.getRef());
						Iterator<Object> succNodes = A.heapGraph.getSuccNodes(pk);
						while (succNodes.hasNext()) {
							InstanceKey o = (InstanceKey) succNodes.next();
							ReadFieldAccess w = new ReadFieldAccess(n, pi, o, f);
							add(w);
						}
					} else {
						ReadFieldAccess w = new ReadFieldAccess(n, pi, null, f);
						add(w);
					}
				}

				if (i instanceof SSAPutInstruction) {
					SSAPutInstruction pi = (SSAPutInstruction) i;
					IField f = A.cha.resolveField(pi.getDeclaredField());
					if (!pi.isStatic()) {
						LocalPointerKey pk = A.pointerForValue.get(n, pi.getRef());
						Iterator<Object> succNodes = A.heapGraph.getSuccNodes(pk);
						while (succNodes.hasNext()) {
							InstanceKey o = (InstanceKey) succNodes.next();
							WriteFieldAccess w = new WriteFieldAccess(n, pi, o, f);
							add(w);
						}
					}else {
						WriteFieldAccess w = new WriteFieldAccess(n, pi, null, f);
						add(w);
					}
				}
				
				if(i instanceof SSAArrayStoreInstruction) {
					SSAArrayStoreInstruction asi = (SSAArrayStoreInstruction) i;
					LocalPointerKey pk = A.pointerForValue.get(n, asi.getArrayRef());
					Iterator<Object> succNodes = A.heapGraph.getSuccNodes(pk);
					IField f = ArrayContents.v();
					while (succNodes.hasNext()) {
						InstanceKey o = (InstanceKey) succNodes.next();
						if (!U.isMainContext(o)) {
							ReadFieldAccess w = new ReadFieldAccess(n, asi, o, f);
							add(w);
						}
					}
				}
			}
		};
	}

	

	@Override
	protected boolean rightIteration(FlexibleContext c) {
		return !((Boolean) c.getItem(CS.MAIN_ITERATION));
	}
}
