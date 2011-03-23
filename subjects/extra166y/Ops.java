package extra166y;

public class Ops {
	public interface Procedure<T> {
		public void op(T b);
	}

	public interface Generator<T> {
		public T op();
	}
}
