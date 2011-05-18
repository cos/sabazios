package sabazios.util;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.util.debug.UnimplementedError;

public class CodeLocation {
	private final String klass;
	private final int lineNo;
	private final String packageName;
	private final String method;

	public CodeLocation(String packageName, String klass, String method,
			int lineNo) {
		this.packageName = packageName;
		this.klass = klass;
		this.method = method;
		this.lineNo = lineNo;
	}

	public CodeLocation(CodeLocation oldCodeLocation, int lineNo) {
		this.packageName = oldCodeLocation.packageName;
		this.klass = oldCodeLocation.klass;
		this.method = oldCodeLocation.method;
		this.lineNo = lineNo;
	}

	@Override
	public String toString() {
		return (packageName + "." + klass).replace('/', '.') + "." + method
				+ "(" + klass.toString().split("\\$")[0] + ".java:"
				+ getLineNo() + ")";
//		return 
//		"(" +packageName+"/"+ klass.toString().split("\\$")[0] + ".java:"
//		+ getLineNo() + ")";
	}

	public String getFullClassName() {
		return packageName + "." + klass;
	}

	public int getLineNo() {
		return lineNo;
	}

	public static CodeLocation make(CGNode cgNode,
			SSAInstruction instruction) {

		if(instruction == null)
			return null;
		try {
			int i = getSSAInstructionNo(cgNode, instruction);
			ShrikeBTMethod method = (ShrikeBTMethod) cgNode.getMethod();
			int bytecodeIndex;

			bytecodeIndex = method.getBytecodeIndex(i);

			return make(cgNode, bytecodeIndex);
		} catch (InvalidClassFileException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			//
		} catch (ClassCastException e) {
			// e.printStackTrace();
		} 
		return null;
	}
	
	public static CodeLocation make(CGNode cgNode,
			int bytecodeIndex) {

		try {
			ShrikeBTMethod method = (ShrikeBTMethod) cgNode.getMethod();

			return make(method, bytecodeIndex);

			// return
			// declaringClass.getName().toString().substring(1).replace('/',
			// '.')+"."+method.getName().toString()+"("+declaringClass.getName().getClassName().toString()+".java:"+lineNumber+")";
		} catch (ArrayIndexOutOfBoundsException e) {
			//
		} catch (ClassCastException e) {
			// e.printStackTrace();
		} 
		return null;
	}

	public static CodeLocation make(ShrikeBTMethod method, int bytecodeIndex) {
		int lineNumber = method.getLineNumber(bytecodeIndex);

		IClass declaringClass = method.getDeclaringClass();

		return new CodeLocation(declaringClass.getName().getPackage()
				.toString(), declaringClass.getName().getClassName()
				.toString(), method.getName().toString(), lineNumber);
	}
	
	public static CodeLocation make(Statement st) {
		if(st.getNode().getMethod() instanceof SummarizedMethod)
			return null;
		
		try{
		ShrikeBTMethod method = (ShrikeBTMethod) st.getNode().getMethod();
		
		int lineNumber = getLineNumber(st);
		
		IClass declaringClass = method.getDeclaringClass();
		return new CodeLocation(declaringClass.getName().getPackage()
				.toString(), declaringClass.getName().getClassName()
				.toString(), method.getName().toString(), lineNumber);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static int getSSAInstructionNo(CGNode cgNode,
			SSAInstruction instruction) {
		int i;
		SSAInstruction[] instructions = cgNode.getIR().getInstructions();
		for (i = 0; i < instructions.length; i++) {
			if (instructions[i] == instruction) {
				break;
			}
		}
		return i;
	}

	public static String getCodeLocationString(CGNode cgNode,
			SSAInstruction instr) {
		CodeLocation codeLocation = CodeLocation
				.make(cgNode, instr);
		String codeLocationString = codeLocation == null ? "E " : codeLocation
				.toString();
		return codeLocationString;
	}

	public static String variableName(Integer v, CGNode cgNode,
			SSAInstruction instruction) {
		if(instruction instanceof SSAPhiInstruction)
			return null;
		
		int ssaInstructionNo = CodeLocation.getSSAInstructionNo(cgNode,
				instruction);
		return variableName(v, cgNode, ssaInstructionNo);
	}

	public static String variableName(Integer v, CGNode cgNode,
			int ssaInstructionNo) {
		String[] localNames;
		try {
			localNames = cgNode.getIR().getLocalNames(ssaInstructionNo,
					v);
		} catch (Exception e) {
			localNames = null;
		} catch (UnimplementedError e) {
			localNames = null;
		} 
		String variableName = null;
		if (localNames != null && localNames.length > 0)
			variableName = localNames[0];
		return variableName;
	}
	
	public static int getLineNumber(Statement s) {
		if (s.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of
													// statements
			int bcIndex, instructionIndex = ((NormalStatement) s)
					.getInstructionIndex();
			try {
				bcIndex = ((ShrikeBTMethod) s.getNode().getMethod())
						.getBytecodeIndex(instructionIndex);
				try {
					return s.getNode().getMethod().getLineNumber(bcIndex);
					// System.err.println ( "Source line number = " );
				} catch (Exception e) {
					System.err.println("Bytecode index no good");
					System.err.println(e.getMessage());
				}
			} catch (Exception e) {
//				System.err
//						.println("it's probably not a BT method (e.g. it's a fakeroot method)");
//				System.err.println(e.getMessage());
			}
		}
		return -1;
	}

	public static CodeLocation make(CGNode node, NewSiteReference site) {
		return make(node, site.getProgramCounter());
	}
	
	public static CodeLocation make(CGNode node, CallSiteReference site) {
		return make(node, site.getProgramCounter());
	}

	public static CodeLocation make(AllocationSiteInNode asin) {
		return make(asin.getNode(), asin.getSite());
	}

	public static CodeLocation make(IMethod iMethod, CallSiteReference site) {
		if(iMethod instanceof ShrikeBTMethod)
			return make((ShrikeBTMethod)iMethod, site.getProgramCounter());
		else
			return null;
	}

	public static Object make(CGNode n) {
		ShrikeCTMethod m = (ShrikeCTMethod) n.getMethod();
		int lineNumber = m.getLineNumber(0)-1;

		IClass declaringClass = m.getDeclaringClass();

		if(lineNumber == 0)
			return null;
		
		return new CodeLocation(declaringClass.getName().getPackage()
				.toString(), declaringClass.getName().getClassName()
				.toString(), m.getName().toString(), lineNumber);
	}
}
