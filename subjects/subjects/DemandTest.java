package subjects;

public class DemandTest {
	class Foo { }
	Foo a;
	
	public void main()
	{
		move();
		Foo x = a;
		move();
		Foo y = a;
	}
	
	public void move()
	{
		a = new Foo();
	}
}
