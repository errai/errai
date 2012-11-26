package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import org.jboss.errai.ioc.client.api.Disposer;
import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton @LoadAsync
public class Foo {
  @Inject Bar bar;
  @Inject Bar2 bar2;
  @Inject Disposer<Bar> barDisposer;
  @Inject BazTheSingleton bazTheSingleton;

  public Bar getBar() {
    return bar;
  }

  public Bar2 getBar2() {
    return bar2;
  }

  public Disposer<Bar> getBarDisposer() {
    return barDisposer;
  }

  public BazTheSingleton getBazTheSingleton() {
    return bazTheSingleton;
  }
}
