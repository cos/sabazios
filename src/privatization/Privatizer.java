package privatization;

import java.util.HashSet;
import java.util.IdentityHashMap;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Privatizer {
  private static IdentityHashMap[] data = new IdentityHashMap[10000];

  private static IdentityHashMap privateObjectsContainer() {
    int threadId = (int) Thread.currentThread().getId();
    IdentityHashMap identityHashMap = data[threadId];
    if (identityHashMap == null) {
      identityHashMap = new IdentityHashMap();
      data[threadId] = identityHashMap;
    }
    return identityHashMap;
  }

  static Object get(Object o) {
    if (o == null)
      return null;

    IdentityHashMap objectContainer = privateObjectsContainer();
    Object obj = objectContainer.get(o);
    if (obj != null)
      return obj;

    Object p = null;

    if (o instanceof Privatizable)
      p = privatizePrivatizable(objectContainer, (Privatizable) o);

    if (o instanceof HashSet)
      p = privatizeHashSet(o);

    if (p == null)
      throw new RuntimeException("Couldn't recognize object type " + o.getClass());

    objectContainer.put(o, p);
    return p;
  }

  // Methods that know how to privatize objects of different classes
  private static <T extends Privatizable<T>> T privatizePrivatizable(IdentityHashMap objectContainer, Privatizable<T> o) {
    T p = o.createPrivate();
    objectContainer.put(o, p);
    o.populatePrivate(p);
    return p;
  }

  private static Object privatizeHashSet(Object o) {
    HashSet ho = (HashSet) o;
    HashSet x = new HashSet();
    for (Object object : ho)
      x.add(get(object));
    return x;
  }
}
