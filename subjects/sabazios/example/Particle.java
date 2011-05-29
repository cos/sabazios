package sabazios.example;
import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

class Particle {
	
	protected static final double dT = 1;
	private static int noSteps = 1000;
	protected double x, y, vX, vY, fX, fY, m;

	public static void main(String[] args) {
		
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[1000], ParallelArray.defaultExecutor()); 
		
		particles.replaceWithGeneratedValue(new Generator<Particle>() {
			public Particle op() {
				Particle p = new Particle();
				readParticle(p);
				return p;
			}
		});
		for (int i = 0; i < noSteps; i++) {
			// ... update force ...

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
					computeCenterOfMass();
				}
			});
		}
	}

	protected static void readParticle(Particle p) {
		
	}
}