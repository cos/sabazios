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
		public Mumu next = null;
		
	}

	public void simple1() {
		Bubu b = new Bubu();
	}
	
	public void simple1Label() {
		Bubu b = new Bubu();
		b.m1 = new Mumu();
		Mumu v1 = b.m1;
	}
	
	public void simpleWithUninteresting() {
		Mumu mumu = new Mumu();
		
		Bubu b = new Bubu();
		b.m1 = mumu;
		b.m2 = mumu;
		Mumu v1 = b.m1;
		v1.x = 10;
	}
	
	public void simplePhi() {
		Mumu mumu = new Mumu();
		
		Bubu b = new Bubu();
		b.m1 = mumu;
		b.m2 = mumu;
		Mumu v1;
		if (mumu.x == 21)
			v1 = b.m1;
		else
			v1 = b.m2;
		v1.x = 10;
	}
	
	public void simpleCalls() {
		Mumu mumu = new Mumu();
		
		Bubu b = new Bubu();
		b.m1 = mumu;
		b.m2 = mumu;
		writeField(b);
	}

	private void writeField(Bubu b) {
		Mumu v1 = b.m1;
		v1.x = 10;
	}
	
	public void simpleCalls2() {
		Mumu mumu = new Mumu();
		
		Bubu b = new Bubu();
		writeField2(mumu, b);
	}

	private void writeField2(Mumu mumu, Bubu b) {
		b.m1 = mumu;
		b.m2 = mumu;
		Mumu v1 = b.m1;
		v1.x = 10;
	}
	
	public void simpleWithReturn() {
		Mumu mumu = new Mumu();
		
		Bubu b = makeBubu(mumu);
		Mumu v1 = b.m1;
		v1.x = 10;
	}

	private Bubu makeBubu(Mumu mumu) {
		Bubu b = new Bubu();
		b.m1 = mumu;
		b.m2 = mumu;
		return b;
	}
	
	//doesn't work on context insensitive
	public void simpleCalls3() {
		Mumu mumu = new Mumu();
		
		Bubu b = new Bubu();
		b.m1 = mumu;
		b.m2 = mumu;
		writeField(b);
		
		Bubu b1 = new Bubu();
		b1.m1 = mumu;
		writeField(b1);
	}
	
	public void simpleWithReturn2() {
		Mumu mumu = new Mumu();
		
		Bubu b1 = makeBubu(mumu);
		Bubu b = makeBubu(mumu);
		Mumu v1 = b.m1;
		v1.x = 10;
	}
	
	public void simpleRecursive() {
		Mumu mumu = new Mumu();
		
		Bubu b = null;
		while(mumu.x > 0) {			
			b = new Bubu();
			b.m1 = mumu;
			b.m2 = mumu;
		}
		
		Mumu v1 = b.m1;
		v1.x = 10;
	}
	
	public void simpleRecursive2() {
		Mumu p = new Mumu();
		
		while(p.x > 0) {			
			Mumu p1 = new Mumu();
			p.next = p1;
			p = p1;
		}
		
		p.x = 10;
	}
}