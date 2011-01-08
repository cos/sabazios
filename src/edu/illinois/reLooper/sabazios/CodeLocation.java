package edu.illinois.reLooper.sabazios;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
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

	public String toString() {
		return (packageName + "." + klass).replace('/', '.') + "." + method
				+ "(" + klass.toString().split("\\$")[0] + ".java:"
				+ getLineNo() + ")";
	}

	public String getFullClassName() {
		return packageName + "." + klass;
	}

	public int getLineNo() {
		return lineNo;
	}

	public static CodeLocation make(CGNode cgNode,
			SSAInstruction instruction) {

		try {
			int i = getSSAInstructionNo(cgNode, instruction);
			ShrikeBTMethod method = (ShrikeBTMethod) cgNode.getMethod();
			int bytecodeIndex;

			bytecodeIndex = method.getBytecodeIndex(i);

			return make(cgNode, method.getLineNumber(bytecodeIndex));
			// return
			// declaringClass.getName().toString().substring(1).replace('/',
			// '.')+"."+method.getName().toString()+"("+declaringClass.getName().getClassName().toString()+".java:"+lineNumber+")";
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

			int lineNumber = method.getLineNumber(bytecodeIndex);

			IClass declaringClass = method.getDeclaringClass();

			return new CodeLocation(declaringClass.getName().getPackage()
					.toString(), declaringClass.getName().getClassName()
					.toString(), method.getName().toString(), lineNumber);

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

	public static String variableName(Integer destValue, CGNode cgNode,
			SSAInstruction instruction) {
		if(instruction instanceof SSAPhiInstruction)
			return null;
		
		int ssaInstructionNo = CodeLocation.getSSAInstructionNo(cgNode,
				instruction);
		String[] localNames;
		try {
			localNames = cgNode.getIR().getLocalNames(ssaInstructionNo,
					destValue);
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
				System.err
						.println("it's probably not a BT method (e.g. it's a fakeroot method)");
				System.err.println(e.getMessage());
			}
		}
		return -1;
	}

	public static CodeLocation make(Statement st) {
		ShrikeBTMethod method = (ShrikeBTMethod) st.getNode().getMethod();

		int lineNumber = getLineNumber(st);

		IClass declaringClass = method.getDeclaringClass();
		return new CodeLocation(declaringClass.getName().getPackage()
				.toString(), declaringClass.getName().getClassName()
				.toString(), method.getName().toString(), lineNumber);
	}
}
