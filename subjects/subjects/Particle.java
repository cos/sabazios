package subjects;
import extra166y.Ops;
import extra166y.ParallelArray;

public class Particle {
	double x, y;
	Particle origin;

	public void moveTo(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void vacuouslyNoRace() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
			}
		});
	}
	
	public void noRaceOnParameter() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
				b.x = 10;
			}
		});
	}
	
	public void noRaceOnParameterInitializedBefore() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>(){
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

	public void raceOnParameterInitializedBefore() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());
		
		final Particle shared = new Particle();
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>(){
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

	public void verySimpleRace() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());
		
		final Particle shared = new Particle();
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>(){
			@Override
			public Particle op() {
				shared.x = 10;
				return new Particle();
			}
		});
	}

}