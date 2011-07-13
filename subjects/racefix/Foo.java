package racefix;

import java.util.HashSet;

import extra166y.Ops;
import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

public class Foo {

  public static class Dog {
    public Cat chases;
    public Cat loves;
  }

  public static class Cat {
    public int lives;
    public Cat follows = null;

  }

  public void simple1() {
    Dog b = new Dog();
  }

  public void simpleLabel() {
    Dog rex = new Dog();
    rex.chases = new Cat();
    Cat pufi = rex.chases;
  }

  public void simpleLabel1() {
    Dog rex = new Dog();
    Cat pufi = new Cat();
    rex.chases = pufi;
  }

  public void simpleTwoLabelsDeep() {
    Dog rex = new Dog();
    rex.chases = new Cat();
    rex.chases.follows = new Cat();
    Cat fifi = rex.chases.follows;
  }

  public void simpleWithUninteresting() {
    Cat mumu = new Cat();

    Dog rex = new Dog();
    rex.chases = mumu;
    rex.loves = mumu;
    Cat fifi = rex.chases;
    fifi.lives -= 1;
  }

  public void simplePhi() {
    Cat mumu = new Cat();

    Dog rex = new Dog();
    rex.chases = mumu;
    rex.loves = mumu;
    Cat pufi;
    if (mumu.lives == 21)
      pufi = rex.chases;
    else
      pufi = rex.loves;
    pufi.lives = 10;
  }

  public void notSoSimplePhi() {
    Cat mumu = new Cat();

    Dog rex = new Dog();
    rex.chases = mumu;
    rex.loves = mumu;
    Cat pufi;
    if (mumu.lives == 21)
      pufi = rex.chases;
    else if (mumu.lives == 12)
      pufi = rex.loves;
    else
      pufi = new Cat();
    pufi.lives = 10;
  }

  public void simpleCalls() {
    Cat mumu = new Cat();

    Dog rex = new Dog();
    rex.chases = mumu;
    rex.loves = mumu;
    writeField(rex);
  }

  private void writeField(Dog rex) {
    Cat pufi = rex.chases;
    pufi.lives = 10;
  }

  public void simpleCalls2() {
    Cat mumu = new Cat();

    Dog b = new Dog();
    writeField2(mumu, b);
  }

  private void writeField2(Cat mumu, Dog b) {
    b.chases = mumu;
    b.loves = mumu;
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
    b.loves = mumu;
    return b;
  }

  // doesn't work on context insensitive
  public void simpleCalls3() {
    Cat mumu = new Cat();

    Dog b = new Dog();
    b.chases = mumu;
    b.loves = mumu;
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
    while (mumu.lives > 0) {
      b = new Dog();
      b.chases = mumu;
      b.loves = mumu;
    }

    Cat v1 = b.chases;
    v1.lives = 10;
  }

  public void simpleRecursive2() {
    Cat p = new Cat();

    while (p.lives > 0) {
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