package org.jboss.errai.ioc.async.test.client.res;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class Foo {
  @Inject Bar bar;
  @Inject Bar2 bar2;

  public Bar getBar() {
    return bar;
  }

  public Bar2 getBar2() {
    return bar2;
  }
}
