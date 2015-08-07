package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.Disposer;

public class NestedDependentBeanWIthDisposer {

  @Inject
  private NestedDependentBean nestedBean;

  @Inject
  private Disposer<NestedDependentBean> disposer;

  public NestedDependentBean getNestedBean() {
    return nestedBean;
  }

  public void dispose() {
    disposer.dispose(nestedBean);
  }

}
