package org.jboss.errai.ioc.async.test.scopes.dependent.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class SomeSingleton {
  private  int instances;

  @PostConstruct
  void postConstruct() {
    instances++;
  }

  public int getInstances() {
    return instances;
  }

}
