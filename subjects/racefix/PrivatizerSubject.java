package racefix;

import extra166y.Ops;
import extra166y.ParallelArray;

public class PrivatizerSubject {
  public static class PrivatizableParticle {
    public double coordX, coordY, middle;

    public void moveTo(double x, double y) {
      this.coordX = x;
      this.coordY = y;
    }
  }

  PrivatizableParticle origin, origin1;

  public void simpleRace() {
    ParallelArray<PrivatizableParticle> particles = ParallelArray.createUsingHandoff(new PrivatizableParticle[10],
        ParallelArray.defaultExecutor());

    final PrivatizableParticle shared = new PrivatizableParticle();

    particles.apply(new Ops.Procedure<PrivatizableParticle>() {
      @Override
      public void op(PrivatizableParticle b) {
        shared.coordX = 10;
      }
    });
  }

  public void writeReadRace() {
    ParallelArray<PrivatizableParticle> particles = ParallelArray.createUsingHandoff(new PrivatizableParticle[10],
        ParallelArray.defaultExecutor());

    final PrivatizableParticle shared = new PrivatizableParticle();

    particles.apply(new Ops.Procedure<PrivatizableParticle>() {
      @Override
      public void op(PrivatizableParticle b) {
        shared.coordX = 10;
        double y = shared.coordX;
      }
    });
  }

  public void readWriteRace() {
    ParallelArray<PrivatizableParticle> particles = ParallelArray.createUsingHandoff(new PrivatizableParticle[10],
        ParallelArray.defaultExecutor());

    final PrivatizableParticle shared = new PrivatizableParticle();

    particles.apply(new Ops.Procedure<PrivatizableParticle>() {
      @Override
      public void op(PrivatizableParticle b) {
        double y = shared.coordX;
        shared.coordX = 10;
      }
    });
  }
}
