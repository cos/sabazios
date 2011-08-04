package racefix;

import extra166y.Ops;
import extra166y.ParallelArray;

public class PrivatizerSubject {
  public static class Particle {
    public double coordX, coordY, middle;

    public Particle next;

    public void moveTo(double x, double y) {
      this.coordX = x;
      this.coordY = y;
    }
  }

  public static class Render {
    private int width;
    private int height;
    private int maxOddSizeSphere;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int maxZ;
    private int minZ;
    private int slab;
    private int depth;
    private Object mat;
    private int maxSphereCache;
    private double[] coef;

    void render(int[] shades, boolean tScreened, int diameter, int x, int y, int z, double[] coef, int selectedOctant,
        Object mat) {
      if (z == 1)
        return;
      width = 42;
      height = 42;
      if (diameter > maxOddSizeSphere)
        diameter &= ~1;
      // if (g3d.isClippedXY(diameter, x, y))
      // return;
      this.mat = mat;
      int radius = (diameter + 1) >> 1;
      minX = x - radius;
      maxX = x + radius;
      minY = y - radius;
      maxY = y + radius;
      slab = 42;
      depth = 42;
      minZ = z - radius;
      maxZ = z + radius;
      if (maxZ < slab || minZ > depth)
        return;

      if (mat != null) {
        this.coef = coef;
      }
      if (mat != null || diameter > maxSphereCache) {
        if (mat != null) {
          this.mat = null;
        }
      } else {
        int[] ss = new int[2];
        if (minX < 0 || maxX >= width || minY < 0 || maxY >= height || minZ < slab || z > depth)
          foo(ss);
        else
          foo(ss);
      }
    }

    private void foo(Object mock) {
    }
  }

  Particle origin, origin1;

  public void simpleRace() {
    ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
        ParallelArray.defaultExecutor());

    final Particle shared = new Particle();

    particles.apply(new Ops.Procedure<Particle>() {
      @Override
      public void op(Particle b) {
        shared.coordX = 10;
      }
    });
  }

  public void writeReadRace() {
    ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
        ParallelArray.defaultExecutor());

    final Particle shared = new Particle();

    particles.apply(new Ops.Procedure<Particle>() {
      @Override
      public void op(Particle b) {
        shared.coordX = 10;
        double y = shared.coordX;
      }
    });
  }

  public void readWriteRace() {
    ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
        ParallelArray.defaultExecutor());

    final Particle shared = new Particle();

    particles.apply(new Ops.Procedure<Particle>() {
      @Override
      public void op(Particle b) {
        double y = shared.coordX;
        shared.coordX = 10;
      }
    });
  }

  public void fieldSuperstar() {
    ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
        ParallelArray.defaultExecutor());

    final Particle shared = new Particle();
    final Particle oneGuy = new Particle();
    final Particle anotherGuy = new Particle();
    oneGuy.next = shared;
    anotherGuy.next = shared;

    particles.apply(new Ops.Procedure<Particle>() {
      @Override
      public void op(Particle b) {
        Particle theSharedGuy = oneGuy.next;
        Particle theSharedGuyAgain = anotherGuy.next;
        theSharedGuy.coordX = 10;
        theSharedGuyAgain.coordX = 12;
      }
    });
  }

  public void threadLocalOfClassWithComputationTest() {
    ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
        ParallelArray.defaultExecutor());

    origin = new Particle();

    particles.apply(new Ops.Procedure<Particle>() {
      @Override
      public void op(Particle b) {
        Particle theOrigin = origin;
        theOrigin.coordX = 10;
      }
    });
  }

  public void falseLCDInRenderMethodDespiteClearWriteBeforeRead() {
    ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
        ParallelArray.defaultExecutor());

    final Render render = new Render();

    particles.apply(new Ops.Procedure<Particle>() {
      @Override
      public void op(Particle b) {
        render.render(new int[2], true, 42, 42, 42, 42, new double[2], 42, new Object());
      }
    });
  }
}
