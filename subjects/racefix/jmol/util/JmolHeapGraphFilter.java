package racefix.jmol.util;

import racefix.AccessTrace;

import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.util.collections.Filter;

@SuppressWarnings("deprecation")
public class JmolHeapGraphFilter implements Filter<Object> {
  private final AccessTrace[] traces;

  public JmolHeapGraphFilter(AccessTrace[] traces) {
    this.traces = traces;
  }

  @Override
  public boolean accepts(Object o) {
    return !isUnwanted(o) && isWanted(o);
  }

  private boolean isInTrace(Object o) {
    for (AccessTrace trace : traces) {
      if (trace.getinstances().contains(o) || trace.getPointers().contains(o))
        return true;
    }
    return false;
  }

  private boolean isWanted(Object o) {

    if (isInTrace(o))
      return true;

    String temp = o.toString();
    String[] str = { "ShapeRenderer", "SticksRenderer", "Graphics3D", "Circle3D", "Cylinder3D", "Line3D", "plotLine",
        "fillCylinder", "fillSphere", "drawDashed", "getTrimmedLine", "plotLineClipped", "I [", "allocateBuffers" };
    for (String s : str) {
      if (temp.contains(s))
        return true;
    }
    return false;
  }

  private boolean isUnwanted(Object o) {
    if (o instanceof LocalPointerKey) {
      return true;
    }

    String temp = o.toString();
    String str[] = { "Exception", "Assertion", "error", "StackTrace", "Vector3f", "Point3f", "Vector3i", "Point3i",
        "BitSet", "Iterator", "HashTable", "Class", "CurrentThread", "getComponentType", "InternalError", "Sphere3D",
        "Hermite3D", "Normix3D", "Triangle3D", "getShades", "sixdots", "render1" };

    for (String s : str) {
      if (temp.contains(s))
        return true;
    }
    return false;
  }
}
