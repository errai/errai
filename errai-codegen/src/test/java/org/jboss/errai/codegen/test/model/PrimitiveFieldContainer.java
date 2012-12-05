package org.jboss.errai.codegen.test.model;

/**
 * A class that has a field of every primitive type. This is relied upon by the
 * mock type oracle in the GWT codegen tests, but you can use it for other tests
 * if that's convenient.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class PrimitiveFieldContainer {

  // DO NOT RENAME OR REMOVE ANY OF THE FOLLOWING FIELDS

  byte byteField;
  char charField;
  short shortField;
  int intField;
  long longField;
  float floatField;
  double doubleField;

  void voidMethod() {}
}
