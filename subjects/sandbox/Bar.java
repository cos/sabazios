package sandbox;

public class Bar {
	
	class Particle {
		int x;
	}
	
	void foo() {
		Particle a = new Particle();
		int x = 10;
		if(x > 11) {
			a = new Particle();
		}
		int y = a.x;
	}
}
