package racefix;

import java.util.HashSet;

import extra166y.Ops;
import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

public class Foo {
	
	public static class Bubu {
		public Mumu m1;
		public Mumu m2;
	}
	
	public static class Mumu {
		public int x;
		
	}

	public void simple1() {
		Bubu b = new Bubu();
		
	}
	
	public void simple2() {
		Bubu b = new Bubu();
		b.m1 = new Mumu();
		Mumu v1 = b.m1;
	}
	
	public void simple3() {
		Mumu mumu = new Mumu();
		
		Bubu b = new Bubu();
		b.m1 = mumu;
		b.m2 = mumu;
		Mumu v1 = b.m1;
		v1.x = 10;
	}
}