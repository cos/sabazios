package sabazios.deref;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import sabazios.util.CodeLocation;
import sabazios.util.U;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.FieldReference;

public class Deref {
	public final CGNode n;
	public final Integer v;
	public final FieldReference f;

	public Deref(CGNode n, Integer v, FieldReference f) {
		this.n = n;
		this.v = v;
		this.f = f;
	}

	@Override
	public String toString() {
		String variableName = CodeLocation.variableName(v, n, n.getIR().getInstructions().length - 1);
		variableName = "(" + variableName + ")";
		return this.n.getMethod().getName().toString() + "#v" + this.v + variableName + U.tos(f);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj.getClass() != this.getClass())
			return false;
		
		Deref other = (Deref) obj;
		return this.n.equals(other.n) && this.v == other.v && (this.f == null ? other.f == null : this.f.equals(other.f));
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(n).append(v).append(f).hashCode();
	}
}