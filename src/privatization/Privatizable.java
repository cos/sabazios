package privatization;

public interface Privatizable<T extends Privatizable<T>> {
	public T privatize();
}
