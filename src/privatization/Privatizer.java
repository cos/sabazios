package privatization;

import java.util.HashSet;
import java.util.IdentityHashMap;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Privatizer {
	private static  IdentityHashMap[] data = new IdentityHashMap[10000];
	
	public static <T extends Privatizable<T>> T get(Privatizable<T> o) {
		if(o == null) return null;
		
		IdentityHashMap objectContainer = privateObjectsContainer();
		T x = (T) objectContainer.get(o);
		if(x == null) {
			x = o.privatize();
			objectContainer.put(o, x);
			return x;
		} else
			return x;
	}

	private static IdentityHashMap privateObjectsContainer() {
		int threadId = (int) Thread.currentThread().getId();
		IdentityHashMap identityHashMap = data[threadId];
		return identityHashMap;
	}
	
	public static Object get(Object o) {
		if(o == null) return null;
		
		IdentityHashMap objectContainer = privateObjectsContainer();
		Object obj = objectContainer.get(o);
		if(obj != null) return obj;
		
		if(o instanceof HashSet) { 
			Object privateO = privatizeHashSet(o);
			objectContainer.put(o, privateO);
			return privateO;
		}
		
		throw new RuntimeException("Couldn't recognize object type "+o.getClass());
	}

	private static Object privatizeHashSet(Object o) {
		HashSet ho = (HashSet) o;
		HashSet x = new HashSet();
		for (Object object : ho) 
			x.add(get(object));
		return x;
	}
}
