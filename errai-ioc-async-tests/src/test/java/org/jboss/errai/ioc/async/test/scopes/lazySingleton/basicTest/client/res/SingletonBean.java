package org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class SingletonBean {
  public static int instances;

  private boolean postConstructed;
  @PostConstruct
  void onPostConstruct() {
    if(postConstructed)throw new IllegalArgumentException("already postconstructed");
    postConstructed=true;
    instances++;
  }
}
