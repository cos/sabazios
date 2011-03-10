package subjects;
import extra166y.Ops;
import extra166y.ParallelArray;

public class Particle {
	double x, y;
	Particle origin;
	Particle origin1;

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
	
	public void noRaceOnANonSharedField() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());
		
		final Particle shared = new Particle();
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>(){
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
	
	public void OneCFANeeded() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());
		
		final Particle shared = new Particle();
		shared.moveTo(3,4);
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>(){
			@Override
			public Particle op() {
				Particle particle = new Particle();
				particle.moveTo(2, 3);
				return particle;
			}
		});
	}
	
	public void TwoCFANeeded() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());
		
		final Particle shared = new Particle();
		compute(shared);
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>(){
			@Override
			public Particle op() {
				Particle particle = new Particle();
				compute(particle);
				return particle;
			}
		});
	}
	
	public void HighCFANeeded() {
		ParallelArray<Particle> particles = ParallelArray.create(10,
				Particle.class, ParallelArray.defaultExecutor());
		
		final Particle shared = new Particle();
		computeRec(shared);
		
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>(){
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
		int x = 10/7;
		if(x > 2)
			computeRec(particle);
		else
			compute(particle);
	}
	
}