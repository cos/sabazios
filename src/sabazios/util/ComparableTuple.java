package sabazios.util;



public class ComparableTuple<Head extends Comparable<Head>, Tail extends Comparable<Tail>> implements Comparable<ComparableTuple<Head, Tail>> {
	Head head;
	Tail tail;

	public ComparableTuple(Head head, Tail tail) {
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

	public static class Pair<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends ComparableTuple<T1, T2> {
		public Pair(T1 o1, T2 o2) {
			super(o1, o2);
		}

		public T2 p2() {
			return tail;
		}
	}

	public static class Triple<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends ComparableTuple<T1, ComparableTuple<T2, T3>> {
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
	public int compareTo(ComparableTuple<Head, Tail> o) {
		int c = head.compareTo(o.head);
		if(c == 0)
			return tail.compareTo(o.tail);
		else return c;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ComparableTuple<?, ?>))
			return false;
		ComparableTuple<?, ?> tup = (ComparableTuple<?, ?>) obj;
		return head.equals(tup.head) && tail.equals(tup.tail);
	}

	public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> Pair<T1, T2> from(T1 o1, T2 o2) {
		return new Pair<T1, T2>(o1, o2);
	}

	public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> Triple<T1, T2, T3> from(T1 o1, T2 o2, T3 o3) {
		return new Triple<T1, T2, T3>(o1, o2, o3);
	}
}
