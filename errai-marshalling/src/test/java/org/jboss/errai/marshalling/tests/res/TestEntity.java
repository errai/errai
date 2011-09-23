package org.jboss.errai.marshalling.tests.res;

import org.jboss.errai.bus.server.annotations.Portable;
import org.jboss.errai.marshalling.client.api.MapsTo;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Portable
public class TestEntity {
  private final String foo;
  private final String foobar;
  private final String bar;

  private int cachedHashCode = -1;

  public TestEntity(@MapsTo("foo") String foo, @MapsTo("bar") String bar) {
    this.foo = foo;
    this.bar = bar;
    this.foobar = foo + bar;
  }

  public String getFoo() {
    return foo;
  }

  @Override
  public int hashCode() {
    if (cachedHashCode == -1) {
      cachedHashCode = foo.hashCode() + 37 * bar.hashCode();
    }
    return cachedHashCode;
  }
}
