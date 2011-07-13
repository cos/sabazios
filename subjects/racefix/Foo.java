package racefix;

import java.util.HashSet;

import extra166y.Ops;
import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

public class Foo {
	
	public static class Dog {
		public Cat chases;
		public Cat m2;
	}
	
	public static class Cat {
		public int lives;
		public Cat follows = null;
		
	}

	public void simple1() {
		Dog b = new Dog();
		b.chases = null;
	}
	
	public void simple1Label() {
		Dog b = new Dog();
		b.chases = new Cat();
		Cat v1 = b.chases;
	}
	
	public void simpleWithUninteresting() {
		Cat mumu = new Cat();
		
		Dog b = new Dog();
		b.chases = mumu;
		b.m2 = mumu;
		Cat v1 = b.chases;
		v1.lives = 10;
	}
	
	public void simplePhi() {
		Cat mumu = new Cat();
		
		Dog b = new Dog();
		b.chases = mumu;
		b.m2 = mumu;
		Cat v1;
		if (mumu.lives == 21)
			v1 = b.chases;
		else
			v1 = b.m2;
		v1.lives = 10;
	}
	
	public void simpleCalls() {
		Cat mumu = new Cat();
		
		Dog b = new Dog();
		b.chases = mumu;
		b.m2 = mumu;
		writeField(b);
	}

	private void writeField(Dog b) {
		Cat v1 = b.chases;
		v1.lives = 10;
	}
	
	public void simpleCalls2() {
		Cat mumu = new Cat();
		
		Dog b = new Dog();
		writeField2(mumu, b);
	}

	private void writeField2(Cat mumu, Dog b) {
		b.chases = mumu;
		b.m2 = mumu;
		Cat v1 = b.chases;
		v1.lives = 10;
	}
	
	public void simpleWithReturn() {
		Cat mumu = new Cat();
		
		Dog b = makeBubu(mumu);
		Cat v1 = b.chases;
		v1.lives = 10;
	}

	private Dog makeBubu(Cat mumu) {
		Dog b = new Dog();
		b.chases = mumu;
		b.m2 = mumu;
		return b;
	}
	
	//doesn't work on context insensitive
	public void simpleCalls3() {
		Cat mumu = new Cat();
		
		Dog b = new Dog();
		b.chases = mumu;
		b.m2 = mumu;
		writeField(b);
		
		Dog b1 = new Dog();
		b1.chases = mumu;
		writeField(b1);
	}
	
	public void simpleWithReturn2() {
		Cat mumu = new Cat();
		
		Dog b1 = makeBubu(mumu);
		Dog b = makeBubu(mumu);
		Cat v1 = b.chases;
		v1.lives = 10;
	}
	
	public void simpleRecursive() {
		Cat mumu = new Cat();
		
		Dog b = null;
		while(mumu.lives > 0) {			
			b = new Dog();
			b.chases = mumu;
			b.m2 = mumu;
		}
		
		Cat v1 = b.chases;
		v1.lives = 10;
	}
	
	public void simpleRecursive2() {
		Cat p = new Cat();
		
		while(p.lives > 0) {			
			Cat p1 = new Cat();
			p.follows = p1;
			p = p1;
		}
		
		p.lives = 10;
	}
	
	public void simpleWithFieldWrites() {
		Cat mumu = new Cat();
		
		Dog b = new Dog();
		b.chases = mumu;
		b.chases = new Cat();
		Cat v1 = b.chases;
		v1.lives = 10;
	}
	
	// 
	public void simpleWithFieldWrites2() {
		Cat pufi = new Cat();
		
		Dog rex = new Dog();
		Cat cici = new Cat();
		cici.follows = pufi;
		Cat bibi = new Cat();
		Cat x = cici.follows;
		rex.chases = x;
		Cat y = rex.chases;
		y.lives -= 1;
	}
	
	public void simpleWithFieldWrites3() {
		Cat pufi = new Cat();
		
		Dog rex = new Dog();
		Cat cici = new Cat();
		cici.follows = pufi;
		Cat bibi = new Cat();
		rex.chases = cici;
		rex.chases = bibi;
		Cat y = rex.chases;
		Cat z = y.follows;
		z.lives -= 1;
	}
}