package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.MapsTo;

/**
 * Part of the regression tests for ERRAI-595 and ERRAI-596.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class EntityWithConstructorAndMethodMappedLong {

  private final long nativeLongValue;

  // this constructor has to be non-public to provoke JSNI generation
  private EntityWithConstructorAndMethodMappedLong(@MapsTo("nativeLongValue") long value) {
    this.nativeLongValue = value;
  }

  public static EntityWithConstructorAndMethodMappedLong instanceFor(long value) {
    return new EntityWithConstructorAndMethodMappedLong(value);
  }

  // this method has to be non-public to provoke JSNI generation
  long getNativeLongValue() {
    return nativeLongValue;
  }

  @Override
  public String toString() {
    return "EWCAMML:" + nativeLongValue;
  }
}
