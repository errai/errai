package org.jboss.errai.ioc.async.test.client.res;

import org.jboss.errai.ioc.client.api.Disposer;

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
  @Inject Disposer<Bar> barDisposer;

  public Bar getBar() {
    return bar;
  }

  public Bar2 getBar2() {
    return bar2;
  }
}
