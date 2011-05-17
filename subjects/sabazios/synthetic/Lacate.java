package sabazios.synthetic;

import java.util.concurrent.locks.ReentrantLock;

import extra166y.Ops;
import extra166y.ParallelArray;

/*
 * Space that can be spared to maintain line numbers  
 * 
 * 
 * 
 * 
 * 
 */

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

	/**
	 * this checks whether the locks are propagated well through methods
	 */
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

	/**
	 * this checks whether the locks take into account the situation where the
	 * method is called from both a synched and unsynched place
	 */
	public void lockFromBothSynchronizedAndUnsynchronized() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (this) {
					someMethod(shared);
				}
				someMethod(shared);
				return new Particle();
			}

			private void someMethod(final Particle shared) {
				shared.x = 10;
			}
		});
	}

	public void synchronizedMethod() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public synchronized Particle op() {
				shared.x = 10;
				return new Particle();
			}
		});
	}

	public void synchronizedStaticMethod() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				raceBabyRace(shared);
				return new Particle();
			}
		});
	}

	private static synchronized void raceBabyRace(final Particle shared) {
		shared.x = 10;
	}

	public void reenterantLock() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				ReentrantLock l = new ReentrantLock();
				l.lock();
				shared.x = 10;
				l.unlock();
				return new Particle();
			}
		});
	}
}
