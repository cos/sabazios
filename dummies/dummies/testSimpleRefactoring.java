package dummies;

import extra166y.Ops;
import extra166y.ParallelArray;

public class testSimpleRefactoring {
	
	private Particle shared = new Particle();
	
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


		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				shared.coordX = 10.;
			}
		});
	}
}