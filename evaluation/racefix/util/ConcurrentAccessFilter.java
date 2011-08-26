package racefix.util;

import java.util.Set;

import sabazios.domains.ConcurrentFieldAccess;

public interface ConcurrentAccessFilter {
  Set<ConcurrentFieldAccess> filter(Set<ConcurrentFieldAccess> setOfAccesses);
}
