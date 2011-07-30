/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2010-01-22 06:55:13 -0600 (Fri, 22 Jan 2010) $
 * $Revision: 12180 $
 *
 * Copyright (C) 2003-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package racefix.jmol;

import racefix.jmol.mock.JmolRendererInterface;
import racefix.jmol.mock.ModelSet;
import racefix.jmol.mock.Shape;

public abstract class ShapeRenderer {

  // public void finalize() {
  // System.out.println("ShapeRenderer " + shapeID + " " + this + " finalized");
  // }

  protected int myVisibilityFlag;
  protected int shapeID;

  // working values
  protected short madBeg;
  protected short madMid;
  protected short madEnd;
  protected int exportType;
  protected JmolRendererInterface g3d;

  protected void initRenderer() {
  }

  public void render(JmolRendererInterface g3d, ModelSet modelSet, Shape shape) {
    this.g3d = g3d;
    exportType = g3d.getExportType();
    render();
    exportType = Graphics3D.EXPORT_NOT;
  }

  abstract protected void render();

}
