package subjects;

import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

public class HighCFA {
	
	static class Particle {
		double x, y; Particle origin;
		public void moveTo(double x, double y) {
			this.x = x;
			this.y = y;
		}
		public void move()
		{
			this.moveTo(10, 14);
		}
	}
	public void main() {
		computeParticles();
	}

	private void computeParticles() {
		ParallelArray<Particle> particles = ParallelArray.create(10, Particle.class, ParallelArray.defaultExecutor());
		final Particle origin = new Particle();
		final Particle delta = new Particle();
		
		origin.move();

		particles.apply(new Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				Particle p = new Particle();
				p.origin = origin;
				compute(p);
			}

			void compute(Particle p) {
				p.moveTo(17, 21);
				double newX = p.origin.x + p.x * delta.x;
				double newY = p.origin.y + p.y * delta.y;
				p.move();
				p.origin.moveTo(newX, newY);
			}
		});
	}
}
