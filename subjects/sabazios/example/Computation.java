package sabazios.example;

import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

class Computation {

	class Particle {
		double x, y, vX, vY, fX, fY, m;
	}

	Particle centerOfMass = new Particle();
	protected Object lock;

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
					synchronized (lock) {
						centerOfMass.m = oldC.m + p.m;
					}
					centerOfMass.x = (oldC.x * oldC.m + p.x * p.m) / centerOfMass.m;
					centerOfMass.y = (oldC.y * oldC.m + p.y * p.m) / centerOfMass.m;
				}
			});
		}
	}

	private void updateForce() {

	}

	private ParallelArray<Particle> createArray() {
		return null;
	}

	protected void readParticle(Particle p) {

	}

	private static final int noSteps = 0;
	protected static final double dT = 0;
}