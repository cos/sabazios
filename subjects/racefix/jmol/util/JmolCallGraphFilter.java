package racefix.jmol.util;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.collections.Filter;

@SuppressWarnings("deprecation")
public class JmolCallGraphFilter implements Filter<CGNode> {
  @Override
  public boolean accepts(CGNode o) {
    return isWanted(o);
  }

  private boolean isWanted(CGNode o) {
    String temp = o.toString();
    String[] str = { "ShapeRenderer", "fillCylinder", "drawDashed", "getTrimmedLine", "plotLineClipped", "drawLine",
        "plotLine", "plotLineDelta", "allocateBuffers" };
    for (String s : str) {
      if (temp.contains(s))
        return true;
    }
    return false;
  }
}