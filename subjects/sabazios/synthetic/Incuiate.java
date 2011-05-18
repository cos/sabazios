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

public class Incuiate {

	static Particle staticParticle = new Particle();
	
	public void simple() {
		Particle p = new Particle();
		p.origin = new Particle();
		Particle y = p.origin;
		y.origin = Incuiate.staticParticle;
		
//		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
//				ParallelArray.defaultExecutor());
//
//		final Particle shared = new Particle();
//
//		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
//			@Override
//			public Particle op() {
//				synchronized (this) {
//					shared.x = 10;					
//				}
//				return new Particle();
//			}
//		});
	}
}
