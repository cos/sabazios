package sabazios.util;

import sabazios.wala.CS;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallString;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallStringContext;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallStringContextSelector;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;


public class U {
	public static boolean detailedResults = false;
	public static String tos(InstanceKey o) {
		if (o == null)
			return null;
		if (!(o instanceof AllocationSiteInNode))
			return o.toString();
		else {
			AllocationSiteInNode asin = (AllocationSiteInNode) o;
			CodeLocation codeLocation = CodeLocation.make(asin);
			if (U.detailedResults)
				return "" + codeLocation + U.tos(asin.getNode().getContext());
			else
				return "" + codeLocation + " new " + o.getConcreteType().getName().getClassName();
		}
	}

	private static String tos(Context context) {
		if(context instanceof CallStringContext) {
			CallStringContext csc = (CallStringContext) context;
			CallString cs  = (CallString) csc.get(CallStringContextSelector.CALL_STRING);
			return U.tos(cs);
		}
		else 
			return context.toString();
	}

	private static String tos(CallString cs) {
		    StringBuffer str = new StringBuffer("[");
		    CallSiteReference[] sites = cs.getCallSiteRefs();
		    IMethod[] methods = cs.getMethods();
			for (int i = 0; i < sites.length; i++) {
				
			str.append(" ").append(CodeLocation.make(methods[i], sites[i]));
		    }
		    str.append(" ]");
		    return str.toString();
	}

	public static String tos(CallSiteReference s) {
		return s == null ? "" : (s.getDeclaredTarget().getName().toString() + s.getProgramCounter());
	}
	
	public static String toString(Object o) {
		return o == null ? "" : o.toString();
	}

	public static String context(CGNode n) {
		if (!(n.getContext() instanceof FlexibleContext))
			return "[O]";
		FlexibleContext context = (FlexibleContext) n.getContext();
		AllocationSiteInNode array = (AllocationSiteInNode) context.getItem(CS.ARRAY);
		
		return tos(array)+((Boolean) context.getItem(CS.MAIN_ITERATION) ? "- m]" : "- c]");
	}

	public static boolean isAlphaIteration(InstanceKey o) {
		if (!(o instanceof AllocationSiteInNode))
			return false;

		AllocationSiteInNode o1 = (AllocationSiteInNode) o;
		Context context = o1.getNode().getContext();
		if (!(context instanceof FlexibleContext))
			return false;

		FlexibleContext fcontext = (FlexibleContext) context;
		Object item = fcontext.getItem(CS.MAIN_ITERATION);
		if (item == null)
			return false;
		return (Boolean) item;
	}

	public static boolean inApplicationScope(CGNode node) {
		IClassLoader classLoader = node.getMethod().getDeclaringClass().getClassLoader();
		return classLoader.getReference().equals(ClassLoaderReference.Application);
	}

	public static boolean inApplicationScope(IMethod method) {
		IClassLoader classLoader = method.getDeclaringClass().getClassLoader();
		return classLoader.getReference().equals(ClassLoaderReference.Application);
	}

	public static boolean inPrimordialScope(IMethod method) {
		IClassLoader classLoader = method.getDeclaringClass().getClassLoader();
		return classLoader.getReference().equals(ClassLoaderReference.Primordial);
	}

	public static String tos(IMethod method) {
		ShrikeBTMethod m = (ShrikeBTMethod) method;
		
		return CodeLocation.make(m, 0).toString();
	}

	public static String tos(Object o) {
		if(o instanceof FieldReference)
			return tosFieldReference((FieldReference) o);
		else
			return ""+o;
	}

	private static String tosFieldReference(FieldReference o) {
		return "."+o.getName();
	}
	
	public static boolean sameExceptIteration(InstanceKey o1, InstanceKey o2) {
		if(o1 == null)
			return o2 == null;
		if (o1 instanceof AllocationSiteInNode && o2 instanceof AllocationSiteInNode) {
			AllocationSiteInNode ao1 = (AllocationSiteInNode) o1;
			AllocationSiteInNode ao2 = (AllocationSiteInNode) o2;
			CGNode n1 = ao1.getNode();
			CGNode n2 = ao2.getNode();
			return CGNodeUtil.equalsExcept(n1, n2, CS.MAIN_ITERATION);
		} else
			return o1.equals(o2);
	}
}
