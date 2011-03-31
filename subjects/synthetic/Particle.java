package synthetic;

import java.util.HashSet;
import extra166y.Ops;
import extra166y.ParallelArray;

public class Particle {
	double x, y;
	Particle origin;
	Particle origin1;

	public void moveTo(double x, double y) {
		this.x = x; this.y = y;
	}

	public void vacuouslyNoRace() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
			}
		});
	}

	public void noRaceOnParameter() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				b.x = 10;
			}
		});
	}

	public void noRaceOnParameterInitializedBefore() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				return new Particle();
			}
		});

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				b.x = 10;
			}
		});
	}

	public void verySimpleRace() {
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

	public void raceOnParameterInitializedBefore() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				particle.origin = shared;
				return particle;
			}
		});

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				b.origin.x = 10;
			}
		});
	}

	public void noRaceOnANonSharedField() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				particle.origin = shared;
				particle.origin1 = new Particle();
				particle.origin1.x = 10;

				return particle;
			}
		});
	}

	public void oneCFANeeded() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		shared.moveTo(3, 4);

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				particle.moveTo(2, 3);
				return particle;
			}
		});
	}

	public void twoCFANeeded() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		compute(shared);

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				compute(particle);
				return particle;
			}
		});
	}

	public void recursive() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		computeRec(shared);

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				computeRec(particle);
				return particle;
			}
		});
	}

	private void compute(Particle particle) {
		particle.moveTo(2, 3);
	}

	private void computeRec(Particle particle) {
		int x = 10 / 7;
		if (x < 2)
			computeRec(particle);
		else
			compute(particle);
	}

	public void disambiguateFalseRace() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		shared.moveTo(3, 4);

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				particle.moveTo(2, 3);
				shared.moveTo(5, 7);
				return particle;
			}
		});
	}

	public void ignoreFalseRacesInSeqOp() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				shared.x = 10;
				particle.origin = shared;
				particle.origin.y = 10;
				return particle;
			}
		});

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				b.x = 10;
			}
		});
	}

	public void raceBecauseOfOutsideInterference() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		shared.moveTo(3, 4);

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				shared.origin = new Particle();
				shared.origin.x = 10;
				return new Particle();
			}
		});
	}

	public void raceOnSharedObjectCarriedByArray() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		shared.moveTo(3, 4);

		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle p = new Particle();
				shared.origin = new Particle();
				p.origin = shared;
				return p;
			}
		});

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle p) {
				p.origin.origin.moveTo(2, 3);
			}
		});
	}

	public void raceBecauseOfDirectArrayLoad() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();
		particles.getArray()[0] = shared;

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle p = (Particle) particles.getArray()[0];
				p.x = 10;
				return new Particle();
			}
		});
	}

	public void raceOnSharedReturnValue() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				shared.x = 10;
				return shared;
			}
		});
	}

	public void raceOnDifferntArrayIteration() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				return new Particle();
			}
		});

		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				return particles.getArray()[0];
			}
		});

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle p) {
				p.x = 10;
			}
		});
	}
	
	public void noRaceIfFlowSensitive() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				return new Particle();
			}
		});
		
		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle p) {
				p.x = 10;
			}
		});
		
		final Particle s = new Particle();

		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {				
				return s;
			}
		});

	}
	
	public void raceOnDifferntArrayIterationOneLoop() {
		final ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValueSeq(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				return new Particle();
			}
		});
		
		final Particle s = new Particle();
		
		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle p) {
				p.origin.x = 10;
				s.origin = new Particle();
				p.origin = s.origin;
			}
		});
	}
	
	public void verySimpleRaceWithIndex() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
			@Override
			public Particle op(int i) {
				shared.x = 10;
				return new Particle();
			}
		});
	 }
	
	final static Particle staticShared = new Particle();
	
	public void verySimpleRaceToStatic() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
			@Override
			public Particle op(int i) {
				staticShared.x = 10;
				return new Particle();
			}
		});
	 }
	
	public void raceOnSharedFromStatic() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
			@Override
			public Particle op(int i) {
				Particle x = staticShared;
				x.y = 11;
				return new Particle();
			}
		});
	 }
	
	public void raceInLibrary() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final HashSet<Particle> sharedSet = new HashSet<Particle>();
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle particle = new Particle();
				sharedSet.add(particle);
				return particle;
			}
		});
	 }
	
	public void noRaceOnStringConcatenation() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle p = new Particle();
				String bla = "tralala" + p;
				return p;
			}
		});
	 }
	
	// should only report one race on "shared.origin = p"
	public void noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
		
		final Particle shared = new Particle();
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle p = new Particle();
				shared.origin = p;
				p.x = 10;
				return new Particle();
			}
		});
	 }
	
	public void noRaceWhenPrintln() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
		
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				System.out.println("bla");
				System.out.print("bla");
				return new Particle();
			}
		});
	 }
}