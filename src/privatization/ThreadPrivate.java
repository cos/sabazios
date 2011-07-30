package privatization;

public class ThreadPrivate<T extends Privatizable<T>> extends ThreadLocal<T> {

	private static long mainThreadId = Thread.currentThread().getId();
	private T mainThreadObject;
	private boolean mainThreadObjectValueIsSet = false;

	@Override
	final protected T initialValue() {
		long currentThreadId = Thread.currentThread().getId();
		if (currentThreadId != mainThreadId) {
			if(mainThreadObjectValueIsSet)
				return (T) Privatizer.get(mainThreadObject);
			else
				return initValue(); 
		} else {
			mainThreadObjectValueIsSet = true;
			return initValue();
		}
	}

	protected T initValue() {
		return null;
	}

	final public void set(T value) {
		long currentThreadId = Thread.currentThread().getId();
		if (currentThreadId == mainThreadId) {
			mainThreadObjectValueIsSet = true;
			mainThreadObject = value;
		}	
		super.set(value);
	};
}
