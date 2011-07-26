package racefix.jmol;

import racefix.jmol.mock.ModelSet;
import racefix.jmol.mock.Shape;

public class Main {

  public void jmol() {
    Graphics3D g3d = new Graphics3D();
    SticksRenderer renderer = new SticksRenderer();
    renderer.render(g3d, new ModelSet(), new Shape());
  }
}
