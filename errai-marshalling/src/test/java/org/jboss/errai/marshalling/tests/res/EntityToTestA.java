package org.jboss.errai.marshalling.tests.res;


import org.jboss.errai.common.client.api.annotations.MapsTo;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
//@Portable
public class EntityToTestA {
  private final String foo;
  private final String foobar;
  private final String bar;
  private final List<EntityToTestA> entityToTestAList;
  private final EntityToTestA[][] arraysOfEntities;

  private int cachedHashCode = -1;

  public EntityToTestA(@MapsTo("foo") String foo,
                       @MapsTo("bar") String bar,
                       @MapsTo("entityToTestAList") List<EntityToTestA> entityToTestAList,
                       @MapsTo("arraysOfEntities") EntityToTestA[][] arraysOfEntities
  ) {
    this.foo = foo;
    this.bar = bar;
    this.entityToTestAList = entityToTestAList;
    this.foobar = foo + bar;
    this.arraysOfEntities = arraysOfEntities;
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
