package sabazios.synthetic;

import extra166y.Ops;
import extra166y.ParallelArray;

public class Lacate {

	public void noLocks() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				shared.x = 10;
				return new Particle();
			}
		});
	}
	
	public void lockUsingSynchronizedBlock() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (this) {					
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	public void lockUsingSynchronizedBlockInAnotherMethod() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (this) {					
					someMethod(shared);
				}
				return new Particle();
			}

			private void someMethod(final Particle shared) {
				shared.x = 10;
			}
		});
	}
}
