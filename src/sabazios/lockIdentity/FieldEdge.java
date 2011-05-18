package sabazios.lockIdentity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.ibm.wala.types.FieldReference;

public class FieldEdge {
	public final FieldReference f;
	public final boolean isW;

	public FieldEdge(FieldReference f, boolean isW) {
		this.f = f;
		this.isW = isW;
	}

	@Override
	public int hashCode() {
		return (new HashCodeBuilder()).append(f).append(isW).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		FieldEdge other = (FieldEdge) obj;
		return (new EqualsBuilder()).append(f, other.f).append(isW, other.isW).isEquals();
	}
	
	@Override
	public String toString() {
		return " "+(isW?"w":"r")+": "+f.getName();
	}
}
