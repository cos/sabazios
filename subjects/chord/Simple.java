package chord;
import java.util.HashSet;

import synthetic.Particle;

public class Simple {
	
	static Object lock = new Object();

	private static final class Task implements Runnable {
		private Particle p;
		private final HashSet<Particle> sharedSet;

		public Task(Particle p, HashSet<Particle> sharedSet) {
			this.p = p;
			this.sharedSet = sharedSet;
		}

		@Override
		public void run() {
			System.out.println();
			synchronized (lock) {				
				p.x = Math.random();
			}
			this.sharedSet.add(new Particle());
		}
	}

	public static void main(String[] args) throws InterruptedException {
		Particle shared = new Particle();
		HashSet<Particle> sharedSet = new HashSet<Particle>();

		Thread t1 = new Thread(new Task(shared, sharedSet));
		Thread t2 = new Thread(new Task(shared, sharedSet));

		t1.start();
		t2.start();

		sharedSet.clear();
		// shared.x = ;

		t1.join();
		t2.join();
	}
}