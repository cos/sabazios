package racefix;

import com.ibm.wala.demandpa.alg.ContextSensitiveStateMachine.RecursionHandler;

public class StatementOrderSubject {
  
  public class Dog {
    public int age;
    public int gender;
  }
  
  public void testTrueIntraprocedural() {
    Dog rex = new Dog();
    rex.age = 32;
    rex.gender = 1;
  }
  
  public void testFalseIntraprocedural() {
    Dog rex = new Dog();
    rex.gender = 1;
    rex.age = 32;
  }
  
  public void testTrueInterprocedural() {
    Dog rex = new Dog();
    rex.age = 32;
    setGender(rex);
  }
  
  public void testFalseInterprocedural() {
    Dog rex = new Dog();
    setGender(rex);
    rex.age = 32;
  }

  private void setGender(Dog rex) {
    rex.gender = 1;    
  }
  
  private void setAge(Dog rex) {
    rex.age = 432;
  }
  
  public void testRecursiveIntraprocedural() {
    Dog rex = new Dog();
    rex.age = 13;
    while(rex.age > 0)
      setGender(rex);
  }
  
  public void testRecursiveInterprocedural() {
    Dog rex = new Dog();
    rex.age = 13;
    recursiveGenderSet(rex);
  }
  
  private void recursiveGenderSet(Dog rex) {
    if (rex.age > 0)
      recursiveGenderSet(rex);
    else
      rex.gender = 0;
  }

  public void testTrueOnReturnPath() {
    Dog rex = new Dog();
    setAge(rex);
    setGender(rex);
  }
  
  public void testFalseOnReturnPath() {
    Dog rex = new Dog();
    setGender(rex);
    setAge(rex);
  }
  
  //TODO add test for more basic blocks
}
