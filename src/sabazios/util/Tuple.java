package sabazios.util;

import org.apache.commons.lang3.builder.HashCodeBuilder;



public class Tuple<Head, Tail>  {
	Head head;
	Tail tail;

	public Tuple(Head head, Tail tail) {
		this.head = head;
		this.tail = tail;
	}

	public Head head() {
		return head;
	}

	public Tail tail() {
		return tail;
	}

	public Head p1() {
		return head();
	}

	public static class Pair<T1, T2> extends Tuple<T1, T2> {
		public Pair(T1 o1, T2 o2) {
			super(o1, o2);
		}

		public T2 p2() {
			return tail;
		}
	}

	public static class Triple<T1, T2 , T3 > extends Tuple<T1, Tuple<T2, T3>> {
		public Triple(T1 o1, T2 o2, T3 o3) {
			super(o1, new Pair<T2, T3>(o2, o3));
		}

		public T2 p2() {
			return tail().p1();
		}

		public T3 p3() {
			return ((Pair<T2, T3>)tail()).p2();
		}
	}
	
	@Override
	public final boolean equals(Object obj) {
		if(obj == null) return false;
		if (!(obj instanceof Tuple))
			return false;
		Tuple<?, ?> tup = (Tuple<?, ?>) obj;
		return head.equals(tup.head) && tail.equals(tup.tail);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(37, 89).append(this.head).append(this.tail).hashCode();
	}
	
	@Override
	public String toString() {
		return "<"+nakedString()+">";
	}
	private String nakedString() {
		String s = head.toString() + ",";
		if(tail instanceof Tuple)
			s += ((Tuple<?, ?>) tail).nakedString();
		else
			s += tail.toString();
		return s;
	}

	public static <T1 , T2 > Pair<T1, T2> from(T1 o1, T2 o2) {
		return new Pair<T1, T2>(o1, o2);
	}

	public static <T1 , T2 , T3 > Triple<T1, T2, T3> from(T1 o1, T2 o2, T3 o3) {
		return new Triple<T1, T2, T3>(o1, o2, o3);
	}
}
