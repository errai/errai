package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * An enum with mutable state, for testing purposes.
 * <p>
 * <blockquote>
 * An Enum with state?<br>
 * What a sad sight to see!<br>
 * Not in my codebase<br>
 * Who'd allow it? Not me!<br>
 * <p>
 * But the compiler allows it<br>
 * Without even a peep<br>
 * So there's one in our testsuite<br>
 * And now we can sleep.
 * </blockquote>
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public enum EnumWithState {

  THING1, THING2;

  int integerState;
  long longState;
  Object objectReference;

  public int getIntegerState() {
    return integerState;
  }
  public long getLongState() {
    return longState;
  }
  public Object getObjectReference() {
    return objectReference;
  }
  public void setIntegerState(int integerState) {
    this.integerState = integerState;
  }
  public void setLongState(long longState) {
    this.longState = longState;
  }
  public void setObjectReference(Object objectReference) {
    this.objectReference = objectReference;
  }
}
