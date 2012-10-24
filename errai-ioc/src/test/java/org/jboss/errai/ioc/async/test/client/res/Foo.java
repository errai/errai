package org.jboss.errai.ioc.async.test.client.res;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class Foo {
  @Inject Bar bar;

  public Bar getBar() {
    return bar;
  }
}
