package privatization;

public interface Privatizable<T extends Privatizable<T>> {
	public T createPrivate();
	public void populatePrivate(T t);
}