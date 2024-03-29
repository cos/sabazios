package sabazios.example;

import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

class Particle {

	protected static final double dT = 1;
	private static int noSteps = 1000;
	protected double x, y, vX, vY, fX, fY, m;

	Particle centerOfMass = new Particle();

	void compute() {

		ParallelArray<Particle> particles = createArray();

		particles.generate(new Generator<Particle>() {
			public Particle op() {
				Particle p = new Particle();
				readParticle(p);
				return p;
			}
		});
		for (int i = 0; i < noSteps; i++) {

			updateForce();

			particles.apply(new Procedure<Particle>() {
				public void op(Particle p) {
					p.vX += p.fX / p.m * dT;
					p.vY += p.fY / p.m * dT;
					p.x += p.vX * dT;
					p.y += p.vY * dT;
				}
			});

			particles.apply(new Procedure<Particle>() {
				public void op(Particle p) {
					Particle oldC = centerOfMass;
					centerOfMass = new Particle();
					centerOfMass.m = oldC.m + p.m;
					centerOfMass.x = (oldC.x * oldC.m + p.x * p.m) / centerOfMass.m;
					centerOfMass.y = (oldC.y * oldC.m + p.y * p.m) / centerOfMass.m;
				}
			});
		}
	}

	private ParallelArray<Particle> createArray() {
		return ParallelArray.createUsingHandoff(new Particle[1000], ParallelArray.defaultExecutor());
	}

	private void updateForce() {

	}

	protected static void readParticle(Particle p) {

	}

}