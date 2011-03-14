package synthetic;

public class ManuThesis {
	static class A {
		void foo(Object p) { }
	}
	static class B extends A {
		void foo(Object p) { 
			mark(p);
		}
	}
	
	void callFoo(A a, Object p) {
		a.foo(p);
	}
	
	public static void mark(Object p) {
		
	}

	void main() {
		A a = new A();
		B b = new B();
		Object p1 = new Object();
		Object p2 = new Object();
		callFoo(a, p1);
		callFoo(b, p2);
	}
}
