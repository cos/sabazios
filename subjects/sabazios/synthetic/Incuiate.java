package sabazios.synthetic;

import java.util.Vector;
import java.util.regex.Pattern;

import extra166y.Ops;
import extra166y.ParallelArray;

/*
 * Space that can be spared to maintain line numbers  
 * 
 *
 * 
 * 
 */

public class Incuiate {

	static Particle staticParticle = new Particle();

	public void simple() {
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
	
	public void oneLevelLocalVar() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (shared) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	public void stillARace() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (new Particle()) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	public void syncedOnParallelArray() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (particles) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	public void syncedOnSomeField() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (shared.origin) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	public void syncedOnSomeChangedField() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				shared.origin = new Particle();
				synchronized (shared.origin) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	static class Lacat {
	}
	
	Lacat lacatStatic;
	
	public void syncedOnStatic() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (lacatStatic) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	public void syncedOnChangedStatic() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				lacatStatic = new Lacat();
				synchronized (lacatStatic) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	public void vectorDefaultSync() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		final Vector<Particle> v = new Vector<Particle>();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				v.add(new Particle());
				return new Particle();
			}
		});
	}
	
	public void printStream() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				System.out.println("bla");
				return new Particle();
			}
		});
	}
	
	public void regexPatternCompile() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Pattern.compile("somePattern.*");
				return null;
			}
		});
	}
	
	public void systemExit() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				System.exit(0);
				return null;
			}
		});
	}
}
