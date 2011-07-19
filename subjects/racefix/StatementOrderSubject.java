package racefix;

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
