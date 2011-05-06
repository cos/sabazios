package sabazios;

import java.util.Iterator;

import sabazios.domains.WriteFieldAccess;
import sabazios.util.ArrayContents;
import sabazios.util.U;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

import edu.illinois.reLooper.sabazios.CS;
import edu.illinois.reLooper.sabazios.FlexibleContext;

public class WriteAccesses extends ObjectAccesses<WriteFieldAccess> {

	public WriteAccesses(RaceAnalysis a) {
		super(a);
	}

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
//					if (!U.isMainContext(o)) {
						WriteFieldAccess w = new WriteFieldAccess(n, pi, o, f);
						this.add(w);
//					}
				}
			} else {
				WriteFieldAccess w = new WriteFieldAccess(n, pi, null, f);
				this.add(w);
			}
		}
		if(i instanceof SSAArrayStoreInstruction) {
			SSAArrayStoreInstruction asi = (SSAArrayStoreInstruction) i;
			LocalPointerKey pk = a.pointerForValue.get(n, asi.getArrayRef());
			Iterator<Object> succNodes = a.heapGraph.getSuccNodes(pk);
			IField f = ArrayContents.v();
			while (succNodes.hasNext()) {
				InstanceKey o = (InstanceKey) succNodes.next();
				if (!U.isMainContext(o)) {
					WriteFieldAccess w = new WriteFieldAccess(n, asi, o, f);
					this.add(w);
				}
			}
		}
	}

	@Override
	protected boolean rightIteration(FlexibleContext c) {
		return ((Boolean) c.getItem(CS.MAIN_ITERATION));
	}
}
