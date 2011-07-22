package sabazios;

import sabazios.util.wala.viz.NodeDecorator;

import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReturnValueKey;
import com.ibm.wala.util.warnings.WalaException;

public class HeapGraphNodeDecorator implements NodeDecorator {

	@Override
	public String getLabel(Object obj) throws WalaException {
		if(obj instanceof AllocationSiteInNode) {
			AllocationSiteInNode o = (AllocationSiteInNode) obj;
			return o.getSite().getDeclaredType().getName().getClassName() + " [ "+o.getNode().getMethod().getName().toString() +  "@" + o.getSite().getProgramCounter()+" ]";
		}
		if(obj instanceof InstanceFieldKey) {
			InstanceFieldKey f = (InstanceFieldKey) obj;
			return f.getField().getName().toString();
		}
		if(obj instanceof LocalPointerKey) {
			LocalPointerKey p = (LocalPointerKey) obj;
			return p.getNode().getMethod().getName().toString() + "-v" + p.getValueNumber();
		}
		if(obj instanceof ReturnValueKey) {
			ReturnValueKey p = (ReturnValueKey) obj;
			return "RET "+p.getNode().getMethod().getName();
		}
			
		return obj.toString();
	}

	@Override
	public String getDecoration(Object obj) {
		if(obj instanceof PointerKey)
			return "shape=box";
		return "";
	}
}
