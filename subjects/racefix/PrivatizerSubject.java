package racefix;

import racefix.PrivatizerSubject.Particle;
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

	Particle origin, origin1;

	public void simpleRace() {
		ParallelArray<Particle> particles = ParallelArray
				.createUsingHandoff(new Particle[10],
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
		ParallelArray<Particle> particles = ParallelArray
				.createUsingHandoff(new Particle[10],
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
		ParallelArray<Particle> particles = ParallelArray
				.createUsingHandoff(new Particle[10],
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
		ParallelArray<Particle> particles = ParallelArray
				.createUsingHandoff(new Particle[10],
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
		ParallelArray<Particle> particles = ParallelArray
				.createUsingHandoff(new Particle[10],
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
}
