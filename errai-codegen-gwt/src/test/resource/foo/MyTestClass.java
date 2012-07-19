package foo;

import java.lang.String;

public class MyTestClass extends MyTestSuperClass {
  private String name;
  private int age;

  public MyTestClass(String name, int age) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }
}