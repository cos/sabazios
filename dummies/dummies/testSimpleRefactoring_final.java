package dummies;

import extra166y.Ops.Procedure;
import extra166y.ParallelArray;
import privatization.ThreadPrivate;

public class testSimpleRefactoring {
	
	private ThreadPrivate<Particle> shared = new ThreadPrivate<Particle>() {
		protected Particle initialValue() {
			return new Particle();
		}
	};
	
	public static class Particle {
		public double coordX, coordY, middle;
		
		public Particle next;

		public void moveTo(double x, double y) {
			this.coordX = x;
			this.coordY = y;
		}
	}
	
	public void simpleRace() {
		ParallelArray<Particle> particles = ParallelArray
				.createUsingHandoff(new Particle[10],
						ParallelArray.defaultExecutor());


		particles.apply(new Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				shared.get().coordX = 10.;
			}
		});
	}
}