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
    PostConstructTestUtil.record(PostConstrBeanB.class.getName());
  }
}
