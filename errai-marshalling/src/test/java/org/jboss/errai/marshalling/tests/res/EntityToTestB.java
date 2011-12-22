package org.jboss.errai.marshalling.tests.res;

import java.util.Date;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
//@Portable
public class EntityToTestB {
//  private String foo;
//  private int num;
//  private EnumTestA testEnum;
  private Date date;

  public EntityToTestB() {
  }
//
//  public EntityToTestB(String foo, int num, EnumTestA testEnum) {
//    this.foo = foo;
//    this.num = num;
//    this.testEnum = testEnum;
//  }

//  public EnumTestA getTestEnum() {
//    return testEnum;
//  }
//
//  public String getFoo() {
//    return foo;
//  }
//
//  public int getNum() {
//    return num;
//  }

  public Date getDate() {
    return date;
  }
}
