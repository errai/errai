package org.jboss.errai.marshalling.tests.res;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Portable
public class Entity {
  private final String foo;
  private final String foobar;
  private final String bar;
//  private final List<TestEntity> entityList;

  private int cachedHashCode = -1;

  public Entity(@MapsTo("foo") String foo, @MapsTo("bar") String bar
  //        , @MapsTo("entityList") List<TestEntity> entityList
  ) {
    this.foo = foo;
    this.bar = bar;
  //  this.entityList = entityList;
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
