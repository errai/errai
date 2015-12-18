package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
public class PostConstrBeanB {
  @Inject
  PostConstrBeanC postConstrBeanC;

  @PostConstruct
  private void postConstr() {
    // Trigger bean creation since all beans are lazily loaded.
    postConstrBeanC.noop();
    PostConstructTestUtil.record(PostConstrBeanB.class.getName());
  }

  public void noop() {}
}
