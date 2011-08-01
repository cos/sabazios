package racefix.util;

import racefix.AccessTrace;

import com.ibm.wala.util.collections.Filter;

@SuppressWarnings("deprecation")
public class AccessTraceFilter implements Filter<Object> {
  private final AccessTrace[] traces;

  public AccessTraceFilter(AccessTrace[] traces) {
    this.traces = traces;
  }

  @Override
  public boolean accepts(Object o) {
    for (AccessTrace trace : traces) {
      if ((trace.getinstances().contains(o) || trace.getPointers().contains(o)) == true)
        return true;
    }
    return false;
  }
}