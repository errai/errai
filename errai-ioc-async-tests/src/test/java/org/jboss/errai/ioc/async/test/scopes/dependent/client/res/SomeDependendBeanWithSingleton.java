package org.jboss.errai.ioc.async.test.scopes.dependent.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class SomeDependendBeanWithSingleton {
  private int instances;
  @Inject
  private SomeSingleton someSingleton;

  @PostConstruct
  void post() {
    assert (getInstances() < someSingleton.getInstances()) : "SomeSIngleton must be created before dependend bean "+getInstances() + " + "+someSingleton.getInstances();
    instances++;
  }

  public int getInstances() {
    return instances;
  }
}
