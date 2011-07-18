package racefix;

import racefix.TraceSubject.Cat;


public class TraceSubject {

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
    pufi.lives = 10; // race
  }

  public void simpleCalls2() {
    Cat pufi = new Cat();

    Dog rex = new Dog();
    writeField2(pufi, rex);
  }

  private void writeField2(Cat mumu, Dog rex) {
    rex.chases = mumu;
    rex.loves = mumu;
    Cat pufi = rex.chases;
    pufi.lives = 10;
  }
  
//doesn't work on context insensitive
 public void simpleCalls3() {
   Cat mumu = new Cat();

   Dog rex = new Dog();
   rex.chases = mumu;
   rex.loves = mumu;
   writeField(rex);

   Dog lassie = new Dog();
   lassie.chases = mumu;
   writeField(lassie);
 }
  
  public void simpleCalls4() {
    Cat mumu = new Cat();

    Dog rex = new Dog();
    rex.chases = mumu;
    rex.loves = mumu;
    Cat pufi = rex.chases;
    writeField4(pufi);
  }

  private void writeField4(Cat pufi) {
    pufi.lives = 10; // race
  }

  public void simpleWithReturn() {
    Cat mumu = new Cat();

    Dog rex = makeDog(mumu);
    Cat pufi = rex.chases;
    pufi.lives = 10;
  }

  private Dog makeDog(Cat pufi) {
    Dog rex = new Dog();
    rex.chases = pufi;
    rex.loves = pufi;
    return rex;
  }

  public void simpleWithReturn2() {
    Cat mumu = new Cat();

    Dog rex = makeDog(mumu);
    rex = makeDog(mumu);
    Cat pufi = rex.chases;
    pufi.lives = 10;
  }
  
  public void simpleWithReturn3() {
    Dog rex = new Dog();
    Cat mumu = new Cat();
    Cat pufi = chaseCat(rex, mumu);
    pufi.lives = 321;
  }

  public Cat chaseCat(Dog rex, Cat mumu) {
    rex.chases = mumu;
    return rex.chases;
  }
  
  public void simpleRecursiveInternal() {
    Cat mumu = new Cat();

    Dog rex = null;
    while (mumu.lives > 0) {
      rex = new Dog();
      rex.chases = mumu;
      rex.loves = mumu;
    }

    Cat pufi = rex.chases;
    pufi.lives = 10;
  }

  public void simpleRecursiveInternal2() {
    Cat pufi = new Cat();

    while (pufi.lives > 0) {
      Cat mumu = new Cat();
      pufi.follows = mumu;
      pufi = mumu;
    }

    pufi.lives = 10;
  }
  
  public void simpleRecursiveExternal() {
    Cat mumu = new Cat();
    
    Cat pufi = recurse(mumu);
    pufi.lives--;
  }

  private Cat recurse(Cat mumu) {
    Dog d = new Dog();
    d.chases = mumu;
    if (mumu.lives > 321)
      return recurse(mumu);
    
    return d.chases;
  }

  public void simpleWithFieldWrites() {
    Cat mumu = new Cat();

    Dog dog = new Dog();
    dog.chases = mumu;
    dog.chases = new Cat();
    Cat pufi = dog.chases;
    pufi.lives = 10;
  }

  //
  public void simpleWithFieldWrites2() {
    Cat mumu = new Cat();

    Dog rex = new Dog();
    Cat cici = new Cat();
    cici.follows = mumu;
    Cat bibi = new Cat(); //noise
    Cat mimi = cici.follows;
    rex.chases = mimi;
    Cat pufi = rex.chases;
    pufi.lives -= 1;
  }

  public void simpleWithFieldWrites3() {
    Cat mumu = new Cat();

    Dog rex = new Dog();
    Cat cici = new Cat();
    cici.follows = mumu;
    Cat bibi = new Cat();
    rex.chases = cici;
    rex.chases = bibi;
    Cat mimi = rex.chases;
    Cat pufi = mimi.follows;
    pufi.lives -= 1;
  }
  
  public void simpleFilter() {
    Dog rex = new Dog();
    rex.chases = new Cat();
    Cat mumu = rex.chases;
    mumu.follows = new Cat();
    blablabla(mumu);
  }

  private void blablabla(Cat mumu) {
    Cat pufi = mumu.follows;
    pufi.lives = 1;
  }
}