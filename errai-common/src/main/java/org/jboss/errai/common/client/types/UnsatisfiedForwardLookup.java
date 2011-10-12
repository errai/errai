package org.jboss.errai.common.client.types;

import java.lang.reflect.Field;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public final class UnsatisfiedForwardLookup {
  private final String id;
  //private Field field;

  public UnsatisfiedForwardLookup(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

//  public Field getField() {
//    return field;
//  }
//
//  public void setField(Field field) {
//    this.field = field;
//  }
}
