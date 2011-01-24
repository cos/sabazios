package subjects;

public class DemandTest {
	class Foo {
		private final int i;

		public Foo(int i) {
			this.i = i;
			
		} }
	Foo a;
	int x = 0;
	
	public void main()
	{
		a = new Foo(7);
		if(Math.random() > 1)
			move();
		else
			move();
		Foo y = a;
	}
	
	public void move()
	{
		a = new Foo(x++);
	}
}
