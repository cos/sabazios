package dummies;

import privatization.ThreadPrivate;
import extra166y.Ops;
import extra166y.ParallelArray;

public class testSimpleRefactoring_final {
	
	public static class Particle {
		public double coordY, middle;
		public ThreadPrivate<Double> coordX = new ThreadPrivate<Double>();
		
		public Particle next;

		public void moveTo(double x, double y) {
			this.coordX.set(x);
			this.coordY = y;
		}
	}
	
	public void simpleRace() {
		ParallelArray<Particle> particles = ParallelArray
				.createUsingHandoff(new Particle[10],
						ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				shared.coordX.set(10.);
			}
		});
	}	
}