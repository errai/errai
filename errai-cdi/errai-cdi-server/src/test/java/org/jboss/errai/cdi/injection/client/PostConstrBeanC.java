package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;

/**
 * @author Mike Brock
 */
public class PostConstrBeanC {
  @PostConstruct
  private void postConstr() {
    PostConstructTestUtil.record(PostConstrBeanC.class.getName());
  }

  public void noop() {}
}
