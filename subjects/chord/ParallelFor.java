package chord;

import java.util.HashSet;

import synthetic.Particle;

public class ParallelFor {

	private static final class Task implements Runnable {
		private Particle p;

		public Task(Particle p) {
			this.p = p;
		}

		@Override
		public void run() {
			System.out.println();
			p.x = p.x + 1;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		Particle shared = new Particle();

		Thread[] t = new Thread[10];
		for (int i = 0; i < 10; i++) {
			t[i] = new Thread(new Task(shared));
			t[i].start();
		}

		for (int i = 0; i < 10; i++) {
			t[i].join();
		}
	}
}