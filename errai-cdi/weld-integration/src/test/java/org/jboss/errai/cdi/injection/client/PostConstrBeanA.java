package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent
public class PostConstrBeanA {
  @Inject
  public PostConstrBeanB postConstrBeanB;

  @PostConstruct
  private void postConstr() {
    PostConstructTestUtil.record(PostConstrBeanA.class.getName());
  }
}
