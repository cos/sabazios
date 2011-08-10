package racefix;

public class StatementOrderSphere3D {
  Graphics3D g3d;

  private static class Graphics3D {
    public int[] zbuf;

    public boolean isClippedXY(int diameter, int x, int y) {
      return false;
    }

  }

  private static class Matrix3f {

    public void transform(Point3f ptTemp) {
    }
  }

  private static class Matrix4f {
  }

  private static class Point3i {

    public int x;
    public int y;
  }

  private static class Point3f {

    public void set(int i, int j, int k) {
    }
  }

  StatementOrderSphere3D(Graphics3D g3d) {
    this.g3d = g3d;
  }

  private int fakeLCDField;

  private final static int maxSphereCache = 128;
  private final static int maxOddSizeSphere = 49;
  final static int maxSphereDiameter = 1000;
  final static int maxSphereDiameter2 = maxSphereDiameter * 2;

  private double[] zroot = new double[2];
  private Matrix3f mat;
  private Matrix4f mDeriv;
  private int selectedOctant;
  private Point3i[] octantPoints;
  private int planeShade;
  private int[] zbuf;
  private int height;
  private int depth;
  private int slab;
  private int offsetPbufBeginLine;
  private boolean addAllPixels;
  private int countSE;

  private int minX, maxX, minY, maxY, minZ, maxZ;
  private int x, y, z, diameter;
  private boolean tScreened;
  private int[] shades;

  public void testRunSphere3D() {
    render(null, true, 42, 42, 42, 42, new Matrix3f(), null, new Matrix4f(), 42, null);
  }

  public void render(int[] shades, boolean tScreened, int diameter, int x, int y, int z, Matrix3f mat, double[] coef,
      Matrix4f mDeriv, int selectedOctant, Point3i[] octantPoints) {
    if (z == 1)
      return;
    fakeLCDField = 42;
    height = 42;
    if (diameter > maxOddSizeSphere)
      diameter &= ~1;
    if (g3d.isClippedXY(diameter, x, y))
      return;
    int radius = (diameter + 1) >> 1;
    minX = x - radius;
    maxX = x + radius;
    minY = y - radius;
    maxY = y + radius;
    depth = maxY;
    minZ = z - radius;
    maxZ = z + radius;
    if (maxZ < slab || minZ > depth)
      return;

    zbuf = g3d.zbuf;
    addAllPixels = true;
    offsetPbufBeginLine = fakeLCDField * y + x;
    this.x = x;
    this.y = y;
    this.z = z;
    this.diameter = diameter;
    this.tScreened = tScreened;
    this.shades = shades;
    this.mat = mat;
    if (mat != null) {
      this.mDeriv = mDeriv;
      this.selectedOctant = selectedOctant;
      this.octantPoints = octantPoints;
    }
    if (mat != null || diameter > maxSphereCache) {
      renderLarge();
      if (mat != null) {
        this.mat = null;
        this.mDeriv = null;
        this.octantPoints = null;
      }
    } else {
      int[] ss = new int[22];
      if (minX < 0 || maxX >= fakeLCDField || minY < 0 || maxY >= height || minZ < slab || z > depth)
        renderShapeClipped(ss);
      else
        uniqueSecondMethodName(ss);
    }
    this.shades = null;
    this.zbuf = null;
    // System.out.println("sphere3d " + nIn + " " + nOut + " " + (1.0 * nIn / (nIn + nOut)));
  }

  private void uniqueSecondMethodName(int[] sphereShape) {
    int uniqueFakeLCDInstructionName = fakeLCDField;
    int offsetSphere = 0;
    int evenSizeCorrection = 1 - (diameter & 1);
    int offsetSouthCenter = offsetPbufBeginLine;
    int offsetNorthCenter = offsetSouthCenter - evenSizeCorrection * uniqueFakeLCDInstructionName;
    int nLines = (diameter + 1) / 2;
    if (!tScreened) {
      do {
        int offsetSE = offsetSouthCenter;
        int offsetSW = offsetSouthCenter - evenSizeCorrection;
        int offsetNE = offsetNorthCenter;
        int offsetNW = offsetNorthCenter - evenSizeCorrection;
        int packed;
        do {
          packed = sphereShape[offsetSphere++];
          int zPixel = z - (packed & 0x7F);
          if (zPixel < zbuf[offsetSE])
            ++offsetSE;
          --offsetSW;
          ++offsetNE;
          --offsetNW;
        } while (packed >= 0);
        offsetSouthCenter += fakeLCDField;
        offsetNorthCenter -= fakeLCDField;
      } while (--nLines > 0);
      return;
    }
    int flipflopSouthCenter = (x ^ y) & 1;
    int flipflopNorthCenter = flipflopSouthCenter ^ evenSizeCorrection;
    int flipflopSE = flipflopSouthCenter;
    int flipflopSW = flipflopSouthCenter ^ evenSizeCorrection;
    int flipflopNE = flipflopNorthCenter;
    int flipflopNW = flipflopNorthCenter ^ evenSizeCorrection;
    int flipflopsCenter = flipflopSE | (flipflopSW << 1) | (flipflopNE << 2) | (flipflopNW << 3);
    do {
      int offsetSE = offsetSouthCenter;
      int offsetSW = offsetSouthCenter - evenSizeCorrection;
      int offsetNE = offsetNorthCenter;
      int offsetNW = offsetNorthCenter - evenSizeCorrection;
      int packed;
      int flipflops = (flipflopsCenter = ~flipflopsCenter);
      do {
        packed = sphereShape[offsetSphere++];
        int zPixel = z - (packed & 0x7F);
        if ((flipflops & 1) != 0 && zPixel < zbuf[offsetSE])
          ++offsetSE;
        --offsetSW;
        ++offsetNE;
        --offsetNW;
        flipflops = ~flipflops;
      } while (packed >= 0);
      offsetSouthCenter += fakeLCDField;
      offsetNorthCenter -= fakeLCDField;
    } while (--nLines > 0);
  }

  private final static int SHADE_SLAB_CLIPPED = 42;

  private void renderShapeClipped(int[] sphereShape) {
    int offsetSphere = 0;
    int evenSizeCorrection = 1 - (diameter & 1);
    int offsetSouthCenter = offsetPbufBeginLine;
    int offsetNorthCenter = offsetSouthCenter - evenSizeCorrection * fakeLCDField;
    int nLines = (diameter + 1) / 2;
    int ySouth = y;
    int yNorth = y - evenSizeCorrection;
    int randu = (x << 16) + (y << 1) ^ 0x33333333;
    int flipflopSouthCenter = (x ^ y) & 1;
    int flipflopNorthCenter = flipflopSouthCenter ^ evenSizeCorrection;
    int flipflopSE = flipflopSouthCenter;
    int flipflopSW = flipflopSouthCenter ^ evenSizeCorrection;
    int flipflopNE = flipflopNorthCenter;
    int flipflopNW = flipflopNorthCenter ^ evenSizeCorrection;
    int flipflopsCenter = flipflopSE | (flipflopSW << 1) | (flipflopNE << 2) | (flipflopNW << 3);
    do {
      boolean tSouthVisible = ySouth >= 0 && ySouth < height;
      boolean tNorthVisible = yNorth >= 0 && yNorth < height;
      int offsetSE = offsetSouthCenter;
      int offsetSW = offsetSouthCenter - evenSizeCorrection;
      int offsetNE = offsetNorthCenter;
      int offsetNW = offsetNorthCenter - evenSizeCorrection;
      int packed;
      int flipflops = (flipflopsCenter = ~flipflopsCenter);
      int xEast = x;
      int xWest = x - evenSizeCorrection;
      do {
        boolean tWestVisible = xWest >= 0 && xWest < fakeLCDField;
        boolean tEastVisible = xEast >= 0 && xEast < fakeLCDField;
        packed = sphereShape[offsetSphere++];
        boolean isCore;
        int zOffset = packed & 0x7F;
        int zPixel;
        if (z < slab) {
          // center in front of plane -- possibly show back half
          zPixel = z + zOffset;
          isCore = (zPixel >= slab);
        } else {
          // center is behind, show front, possibly as solid core
          zPixel = z - zOffset;
          isCore = (zPixel < slab);
        }
        if (isCore)
          zPixel = slab;
        if (zPixel >= slab && zPixel <= depth) {
          if (tSouthVisible) {
            if (tEastVisible && (addAllPixels || (flipflops & 1) != 0) && zPixel < zbuf[offsetSE]) {
              int i = (isCore ? SHADE_SLAB_CLIPPED - 3 + ((randu >> 7) & 0x07) : (packed >> 7) & 0x3F);
            }
            if (tWestVisible && (addAllPixels || (flipflops & 2) != 0) && zPixel < zbuf[offsetSW]) {
              int i = (isCore ? SHADE_SLAB_CLIPPED - 3 + ((randu >> 13) & 0x07) : (packed >> 13) & 0x3F);
            }
          }
          if (tNorthVisible) {
            if (tEastVisible && (!tScreened || (flipflops & 4) != 0) && zPixel < zbuf[offsetNE]) {
              int i = (isCore ? SHADE_SLAB_CLIPPED - 3 + ((randu >> 19) & 0x07) : (packed >> 19) & 0x3F);
            }
            if (tWestVisible && (!tScreened || (flipflops & 8) != 0) && zPixel < zbuf[offsetNW]) {
              int i = (isCore ? SHADE_SLAB_CLIPPED - 3 + ((randu >> 25) & 0x07) : (packed >> 25) & 0x3F);
            }
          }
        }
        ++offsetSE;
        --offsetSW;
        ++offsetNE;
        --offsetNW;
        ++xEast;
        --xWest;
        flipflops = ~flipflops;
        if (isCore)
          randu = ((randu << 16) + (randu << 1) + randu) & 0x7FFFFFFF;
      } while (packed >= 0);
      offsetSouthCenter += fakeLCDField;
      offsetNorthCenter -= fakeLCDField;
      ++ySouth;
      --yNorth;
    } while (--nLines > 0);
  }

  private void renderLarge() {
    if (mat != null) {
      Object ellipsoidShades = null;
      if (ellipsoidShades == null)
        createEllipsoidShades();
      if (octantPoints != null)
        setPlaneDerivatives();
    }
    renderQuadrant(-1, -1);
    renderQuadrant(-1, 1);
    renderQuadrant(1, -1);
    renderQuadrant(1, 1);
  }

  private void createEllipsoidShades() {

  }

  private void renderQuadrant(int xSign, int ySign) {
    int radius = diameter / 2;
    int t = x + radius * xSign;
    int xStatus = (x < 0 ? -1 : x < fakeLCDField ? 0 : 1) + (t < 0 ? -2 : t < fakeLCDField ? 0 : 2);
    if (xStatus == -3 || xStatus == 3)
      return;

    t = y + radius * ySign;
    int yStatus = (y < 0 ? -1 : y < height ? 0 : 1) + (t < 0 ? -2 : t < height ? 0 : 2);
    if (yStatus == -3 || yStatus == 3)
      return;

    boolean unclipped = (mat == null && xStatus == 0 && yStatus == 0 && z - radius >= slab && z <= depth);
    if (unclipped)
      renderQuadrantUnclipped(radius, xSign, ySign);
    else
      renderQuadrantClipped(radius, xSign, ySign);
  }

  private void renderQuadrantUnclipped(int radius, int xSign, int ySign) {
    int r2 = radius * radius;
    int dDivisor = radius * 2 + 1;
    // it will get flipped twice before use
    // so initialize it to true if it is at an even coordinate
    boolean flipflopBeginLine = ((x ^ y) & 1) == 0;
    int lineIncrement = (ySign < 0 ? -fakeLCDField : fakeLCDField);
    int ptLine = offsetPbufBeginLine;
    for (int i = 0, i2 = 0; i2 <= r2; i2 += i + (++i), ptLine += lineIncrement) {
      int offset = ptLine;
      boolean flipflop = (flipflopBeginLine = !flipflopBeginLine);
      int s2 = r2 - i2;
      int z0 = z - radius;
      for (int j = 0, j2 = 0; j2 <= s2; j2 += j + (++j), offset += xSign) {
        if (addAllPixels || (flipflop = !flipflop)) {
          if (zbuf[offset] <= z0)
            continue;
          int k = (int) Math.sqrt(s2 - j2);
          z0 = z - k;
          if (zbuf[offset] <= z0)
            continue;
          int x8 = ((j * xSign + radius) << 8) / dDivisor;
        }
      }
    }
  }

  private final Point3f ptTemp = new Point3f();
  private final int[] planeShades = new int[3];
  private final float[][] dxyz = new float[3][3];

  private void renderQuadrantClipped(int radius, int xSign, int ySign) {
    boolean isEllipsoid = (mat != null);
    boolean checkOctant = (selectedOctant >= 0);
    int r2 = radius * radius;
    int lineIncrement = (ySign < 0 ? -fakeLCDField : fakeLCDField);
    int ptLine = offsetPbufBeginLine;
    int randu = (x << 16) + (y << 1) ^ 0x33333333;
    int yCurrent = y;
    int iShade = 0;
    for (int i = 0, i2 = 0; i2 <= r2; i2 += i + (++i), ptLine += lineIncrement, yCurrent += ySign) {
      if (yCurrent < 0) {
        if (ySign < 0)
          return;
        continue;
      }
      if (yCurrent >= height) {
        if (ySign > 0)
          return;
        continue;
      }
      int s2 = r2 - (isEllipsoid ? 0 : i2);
      int xCurrent = x;
      if (!isEllipsoid) {
      }
      randu = ((randu << 16) + (randu << 1) + randu) & 0x7FFFFFFF;
      int iRoot = -1;
      int mode = 1;
      int offset = ptLine;
      for (int j = 0, j2 = 0; j2 <= s2; j2 += j + (++j), offset += xSign, xCurrent += xSign) {
        if (xCurrent < 0) {
          if (xSign < 0)
            break;
          continue;
        }
        if (xCurrent >= fakeLCDField) {
          if (xSign > 0)
            break;
          continue;
        }
        if (tScreened && (((xCurrent ^ yCurrent) & 1) != 0))
          continue;
        int zPixel;
        if (isEllipsoid) {
          if (iRoot >= 0) {
            // done for this line
            break;
          }
          iRoot = (z < slab ? 1 : 0);
          zPixel = (int) zroot[iRoot];
          if (zPixel == 0)
            zPixel = z;
          mode = 2;
          if (checkOctant) {
            ptTemp.set(xCurrent - x, yCurrent - y, zPixel - z);
            mat.transform(ptTemp);
            int thisOctant = 42;
            if (thisOctant == selectedOctant) {
              iShade = getPlaneShade(xCurrent, yCurrent, zroot);
              zPixel = (int) zroot[0];
              mode = 3;
              // another option: show back only
              // iRoot = 1;
              // zPixel = (int) zroot[iRoot];
            }
          }
        } else {
          int zOffset = (int) Math.sqrt(s2 - j2);
          zPixel = z + (z < slab ? zOffset : -zOffset);
        }
        boolean isCore = (z < slab ? zPixel >= slab : zPixel < slab);
        if (isCore) {
          zPixel = slab;
          mode = 0;
        }
        if (zPixel < slab || zPixel > depth || zbuf[offset] <= zPixel)
          continue;
        switch (mode) {
        case 0: // core
          iShade = (SHADE_SLAB_CLIPPED - 3 + ((randu >> 8) & 0x07));
          randu = ((randu << 16) + (randu << 1) + randu) & 0x7FFFFFFF;
          mode = 1;
          break;
        case 2: // ellipsoid
          break;
        case 3: // ellipsoid fill
          break;
        default: // sphere
          iShade = 42;
          break;
        }
      }
      // randu is failing me and generating moire patterns :-(
      // so throw in a little more salt
      randu = ((randu + xCurrent + yCurrent) | 1) & 0x7FFFFFFF;
    }
  }

  // //////// Ellipsoid Code ///////////
  //
  // Bob Hanson, 4/2008
  //
  // ////////////////////////////////////

  private void setPlaneDerivatives() {
    planeShade = -1;
    for (int i = 0; i < 3; i++) {
      float dx = dxyz[i][0] = octantPoints[i].x - x;
      float dy = dxyz[i][1] = octantPoints[i].y - y;
      planeShades[i] = 42;
      if (dx == 0 && dy == 0) {
        planeShade = planeShades[i];
        return;
      }
    }
  }

  private int getPlaneShade(int xCurrent, int yCurrent, double[] zroot) {
    if (planeShade >= 0)
      return planeShade;
    int iMin = 3;
    float dz;
    float zMin = Float.MAX_VALUE;
    for (int i = 0; i < 3; i++) {
      if ((dz = dxyz[i][2]) == 0)
        continue;
      float ptz = z + (-dxyz[i][0] * (xCurrent - x) - dxyz[i][1] * (yCurrent - y)) / dz;
      if (ptz < zMin) {
        zMin = ptz;
        iMin = i;
      }
    }
    if (iMin == 3) {
      iMin = 0;
      zMin = z;
    }
    zroot[0] = zMin;
    return planeShades[iMin];
  }

}
