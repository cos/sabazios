package extra166y;

import java.util.HashSet;

public class ParallelArray<T> {
	HashSet<T> pseudoArrayWithin = new HashSet<T>();
	
	public static jsr166y.ForkJoinPool defaultExecutor() {
		return null;
	}
	public static <T> ParallelArray<T> create(int size, Class<? super T> elementType, jsr166y.ForkJoinPool executor)
	{
		return new ParallelArray<T>();
	}
	
	public void apply(Ops.Procedure<? super T> procedure) 
	{
		procedure.op(pseudoArrayWithin.iterator().next());
	}
}
