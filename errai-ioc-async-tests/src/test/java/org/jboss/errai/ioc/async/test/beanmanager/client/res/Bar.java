package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@LoadAsync
public class Bar {
  @Inject Foo foo;
  @Inject BazTheSingleton bazTheSingleton;

  private boolean postContr = false;

  @PostConstruct
  private void onPostContruct() {
    postContr = true;
  }

  public Foo getFoo() {
    return foo;
  }

  public BazTheSingleton getBazTheSingleton() {
    return bazTheSingleton;
  }

  public boolean isPostContr() {
    return postContr;
  }
}
