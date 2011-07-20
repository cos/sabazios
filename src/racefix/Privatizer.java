package racefix;

import java.util.Set;

import sabazios.A;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentAccesses;

public class Privatizer {
  
  public static class ClassChangeSet {
    public Set<String> threadLocal;
    public Set<PrivatizeMethod> privatizeMethods;
    
    public static class PrivatizeMethod {
      public Set<String> privatizedFields;
    }
  }
  
  private final A a;
  private final ConcurrentAccesses<ConcurrentAccess<?>> accesses;

  public Privatizer(A a, ConcurrentAccesses<ConcurrentAccess<?>> accesses) {
    this.a = a;
    this.accesses = accesses;
  }
  
  
}
