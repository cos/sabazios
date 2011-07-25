package racefix;


import sabazios.A;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.ConcurrentAccesses;

public class Privatizer {
  
  private final A a;
  private final ConcurrentAccesses<ConcurrentAccess<?>> accesses;

  public Privatizer(A a, ConcurrentAccesses<ConcurrentAccess<?>> accesses) {
    this.a = a;
    this.accesses = accesses;
  }
}
