package sandbox;

public class Flow {
	
	public static void main(String[] args) {
		Flow f = new Flow();
		f.compute();
	}
	
	Object o1;
	Object o2;
	
	private void compute() {
		T1 t1 = new T1(this);
		T2 t2 = new T2(this);
		t1.start();
		t2.start();
	}
	static class T1 extends Thread {
		private final Flow s;

		public T1(Flow s) {
			this.s = s;
		}
		
		@Override
		public void run() {
			s.o2 = s.o1;
		}
	}
	
	static class T2 extends Thread {
		private final Flow s;

		public T2(Flow s) {
			this.s = s;
		}
		
		@Override
		public void run() {
			s.o1 = new Object();
			bla(s.o2);
		}
		private void bla(Object o22) {
			
		}
	}
}
