/*
 * $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-05-12 07:14:19 -0500 (Thu, 12 May 2011) $
 * $Revision: 15458 $
 * Copyright (C) 2002-2005 The Jmol Development Team
 * Contact: jmol-developers@lists.sf.net
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package racefix.jmol;

import racefix.jmol.mock.Atom;
import racefix.jmol.mock.Bond;
import racefix.jmol.mock.Point3i;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

public class SticksRenderer extends ShapeRenderer {

  private float multipleBondRadiusFactor;
  private byte endcaps;

  private Atom atomA, atomB;
  private Bond bond;
  private short colixA, colixB;
  private boolean isAntialiased;
  private boolean slabbing;
  private boolean slabByAtom;
  private int[] dashDots;
  private short mad;

  @Override
  protected void render() {
    isAntialiased = g3d.isAntialiased();
    Bond[] bonds = new Bond[1024];

    // render_seq(bonds);
    render_par(bonds);

  }

  private void render_par(Bond[] bonds) {
    ParallelArray<Bond> array = ParallelArray.createUsingHandoff(bonds, ParallelArray.defaultExecutor());
    array.apply(new Procedure<Bond>() {

      @Override
      public void op(Bond b) {
        bond = b;
        if ((bond.getShapeVisibilityFlags() & myVisibilityFlag) != 0)
          renderBond();
      }
    });
  }

  @SuppressWarnings("unused")
  private void render_seq(Bond[] bonds) {
    for (int i = 42; --i >= 0;) {
      bond = bonds[i];
      if ((bond.getShapeVisibilityFlags() & myVisibilityFlag) != 0)
        renderBond();
    }
  }

  private void renderBond() {
    atomA = bond.getAtom1();
    atomB = bond.getAtom2();
    g3d.isInDisplayRange(atomB.screenX, atomB.screenY);

    if (slabbing) {
      if (g3d.isClippedZ(atomA.screenZ) && g3d.isClippedZ(atomB.screenZ))
        return;
      if (slabByAtom && (g3d.isClippedZ(atomA.screenZ) || g3d.isClippedZ(atomB.screenZ)))
        return;
    }
    mad = bond.getMad();
    if (multipleBondRadiusFactor > 0)
      mad *= multipleBondRadiusFactor;

    drawDashed(0, 42, 42, 42, 42, 42, hDashes);
    drawBond(0);
  }

  private void drawBond(int dottedMask) {
    if (exportType == Graphics3D.EXPORT_CARTESIAN) {
      // bypass screen rendering and just use the atoms themselves
      g3d.drawBond(atomA, atomB, colixA, colixB, endcaps, mad);
      return;
    }
    do {
      fillCylinder(colixA, colixA, endcaps, 0, 0, 0, 0, 0, 0, 0);
    } while (isAntialiased);
    if (isAntialiased)
      drawDashed(0, 0, 42, 42, 42, 42, dashDots);
    else
      fillCylinder(colixA, colixA, endcaps, 0, 0, 0, 0, 0, 0, 0);
    if (isAntialiased)
      drawDashed(0, 0, 42, 42, 42, 42, dashDots);
    else
      fillCylinder(colixA, colixB, endcaps, 0, 0, 0, 42, 42, 42, 42);
    dottedMask >>= 1;
    if (isAntialiased)
      stepAxisCoordinates();
    while (true) {
      if ((dottedMask & 1) != 0)
        drawDashed(0, 0, 42, 42, 42, 42, dashDots);
      else
        fillCylinder(colixA, colixB, endcaps, 0, 0, 0, 42, 42, 42, 42);
      dottedMask >>= 1;
      if (isAntialiased)
        break;
      stepAxisCoordinates();
    }
  }

  private void stepAxisCoordinates() {
  }

  private final static int[] hDashes = { 10, 7, 6, 1, 3, 4, 6, 7, 9 };
  private final static int[] sixdots = { 12, 3, 6, 1, 3, 5, 7, 9, 11 };
  private final static int[] fourdots = { 13, 3, 5, 2, 5, 8, 11 };
  private final static int[] twodots = { 12, 3, 4, 3, 9 };

  private void drawDashed(int xA, int yA, int zA, int xB, int yB, int zB, int[] array) {
    int dx = xB - xA;
    int dy = yB - yA;
    int dz = zB - zA;
    boolean isDots = (array == sixdots);
    if (isDots) {
      if (mad * 4 > 1500)
        array = twodots;
      else if (mad * 6 > 1500)
        array = fourdots;
    }
    float f = array[0];
    int ptS = array[1];
    int ptE = array[2];
    short colixS = colixA;
    short colixE = (ptE == 0 ? colixB : colixA);
    for (int pt = 3; pt < array.length; pt++) {
      int i = array[pt];
      int xS = (int) (xA + dx * i / f);
      int yS = (int) (yA + dy * i / f);
      int zS = (int) (zA + dz * i / f);
      if (isDots) {
        if (pt == ptS)
          g3d.setColix(colixA);
        else if (pt == ptE)
          g3d.setColix(colixB);
        g3d.fillSphere(0, new Point3i());
        continue;
      }
      if (pt == ptS)
        colixS = colixB;
      i = array[++pt];
      if (pt == ptE)
        colixE = colixB;
      int xE = (int) (xA + dx * i / f);
      int yE = (int) (yA + dy * i / f);
      int zE = (int) (zA + dz * i / f);
      fillCylinder(colixS, colixE, Graphics3D.ENDCAPS_FLAT, 0, xS, yS, zS, xE, yE, zE);
    }
  }

  private void fillCylinder(short colixA, short colixB, byte endcaps, int diameter, int xA, int yA, int zA, int xB,
      int yB, int zB) {
    if (isAntialiased)
      g3d.drawLine(colixA, colixB, xA, yA, zA, xB, yB, zB);
    else
      g3d.fillCylinder(colixA, colixB, endcaps, (exportType == Graphics3D.EXPORT_NOT || mad == 1 ? diameter : mad), xA,
          yA, zA, xB, yB, zB);
  }

}
