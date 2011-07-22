package sabazios;

import sabazios.lockset.LockSet;
import sabazios.util.wala.viz.NodeDecorator;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.warnings.WalaException;

public class CGNodeDecorator implements NodeDecorator {
	private final A a;
	
	public CGNodeDecorator(A a) {
		this.a = a;
  }
	
	public String getLabel(Object o) throws WalaException {
		CGNode n = (CGNode) o;
		MethodReference reference = n.getMethod().getReference();
		String s = reference.getDeclaringClass().getName() + "." + reference.getSelector() + " <"+reference.getDeclaringClass().getClassLoader().getName().toString().substring(0, 4)+ ">";
		Context context = n.getContext();
		
		s += context.toString();
		s = s.replace("][", "\\n");
		s = s.replace("[", "\\n");
		s = s.replace("]", "\\n");
		
		LockSet lock = a.locks.get(n);
		s += "\\n"+lock;
		
		return s.substring(1);
	}

	@Override
	public String getDecoration(Object n) {
		return "";
	}

	@Override
	public boolean shouldDisplay(Object n) {
		return true;
	}

}
