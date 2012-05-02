package org.jboss.errai.codegen.test.model;

public class PersonImpl implements Person {

  private String name;
  private int age;
  private Person mother;

  public PersonImpl(String name, int age, Person mother) {
    super();
    this.name = name;
    this.age = age;
    this.mother = mother;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getAge() {
    return age;
  }

  @Override
  public Person getMother() {
    return mother;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public void setMother(Person mother) {
    this.mother = mother;
  }

  @Override
  public String toString() {
    return "Person \"" + name + "\"";
  }
}
