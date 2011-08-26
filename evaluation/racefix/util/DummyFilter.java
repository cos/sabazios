package racefix.util;

import java.util.Set;

import sabazios.domains.ConcurrentFieldAccess;

public class DummyFilter implements ConcurrentAccessFilter {

  @Override
  public Set<ConcurrentFieldAccess> filter(Set<ConcurrentFieldAccess> setOfAccesses) {
    return setOfAccesses;
  }

}
