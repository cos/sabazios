package sabazios.util;

import java.util.HashSet;

public class Relation<Head, Tail> extends HashSet<Tuple<Head, Tail>> {

	private static final long serialVersionUID = -9021413002040520796L;

	public Relation<Head, Tail> restrict(Object h) {
		Relation<Head, Tail> s = new Relation<Head, Tail>();
		for (Tuple<Head, Tail> t : this) {
			if (t.head.equals(h))
				s.add(t);
		}
		return s;
	}

	public Object tail(Object h) {
		HashSet<Object> s = new HashSet<Object>();
		for (Tuple<Head, Tail> t : this) {
			if (t.head.equals(h))
				s.add(t.tail);
		}
		return s;
	}
	
	public Tuple<Head, Tail> any() {
		return this.iterator().next();
	}

//	public static class PairRelation<T1, T2> extends Relation<Pair<T1, T2>> {
//		private static final long serialVersionUID = 1494293056619302144L;
//	}
//
//	public static class TripleRelation<T1, T2, T3> extends Relation<Triple<T1, T2, T3>> {
//		private static final long serialVersionUID = -9221659342921730549L;
//
//		public  PairRelation<T2, T3> tail(T1 h) {
//			PairRelation<T2, T3> s = new PairRelation<T2, T3>();
//			for (Tuple<T1, Pair<T2, T3>> t : this) {
//				if (t.head.equals(h))
//					s.add(t.tail);
//			}
//			return s;
//		}
//	}
}