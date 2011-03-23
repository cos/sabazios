package extra166y;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelArray<T> {
	public static final CharSequence PAROP_STRING = "8888888888888";
	public static final CharSequence SEQOP_STRING = "1111111111111";
	T[] array;
//	private final ExecutorService executor;

	@SuppressWarnings("unchecked")
	private ParallelArray(int size, ExecutorService executor) {
//		this.executor = executor;
		array = (T[]) new Object[size];
	}

	private ParallelArray(T[] source, ExecutorService executor) {
		array = source;
//		this.executor = executor;
	}

	public static ExecutorService defaultExecutor() {
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	public static <T> ParallelArray<T> create(int size, Class<? super T> elementType, ExecutorService executor) {
		return new ParallelArray<T>(size, executor);
	}

	public static <T> ParallelArray<T> createUsingHandoff(T[] source, ExecutorService executor) {
		return new ParallelArray<T>(source, executor);
	}

	public void apply(final Ops.Procedure<? super T> procedure) {
		for (int i = 0; i < array.length; i++) {
			final int j = i;
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					procedure.op(array[j]);
				}
			});
			thread.start();
		}
	}

	public void applySeq(Ops.Procedure<? super T> procedure) {
		for (T e : array) {
			procedure.op(e);
		}
	}

	public void replaceWithGeneratedValue(final Ops.Generator<? super T> generator) {
		for (int i = 0; i < array.length; i++) {
			final int j = i;
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					array[j] = (T) generator.op();
				}
			});
			thread.start();
		}
	}

	public void replaceWithGeneratedValueSeq(Ops.Generator<? super T> generator) {
		for (int i = 0; i < array.length; i++) {
			array[i] = (T) generator.op();
		}
	}

	public T[] getArray() {
		return array;
	}

	public static <T> ParallelArray<T> createFromCopy(T[] array, ExecutorService defaultExecutor) {
		ParallelArray<T> pa = new ParallelArray<T>(array.clone(), defaultExecutor);
		return pa;
	}
}
