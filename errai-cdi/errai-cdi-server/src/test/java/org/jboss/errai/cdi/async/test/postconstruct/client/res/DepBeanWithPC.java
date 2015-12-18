package org.jboss.errai.cdi.async.test.postconstruct.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

/**
 * @author Mike Brock
 */
@Dependent
public class DepBeanWithPC {
  private boolean postConstructCalled = false;

  @PostConstruct
  private void onPost() {
    postConstructCalled = true;
  }

  public boolean isPostConstructCalled() {
    return postConstructCalled;
  }
}
