/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2010-12-06 22:36:47 -0600 (Mon, 06 Dec 2010) $
 * $Revision: 14757 $

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

package racefix.jmol.mock;

public class Bond {

  private Atom atom1;
  private Atom atom2;

  public Bond() {
    this.atom1 = new Atom();
    this.atom2 = new Atom();
  }

  public int getShapeVisibilityFlags() {
    return 0;
  }

  public Atom getAtom1() {
    return this.atom1;
  }

  public Atom getAtom2() {
    return this.atom2;
  }

  public short getMad() {
    return 0;
  }
}
