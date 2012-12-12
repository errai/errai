package org.jboss.errai.ioc.async.test.constructor.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class ConstrInjBean {
  private ConstrInjBean myself;
  private Apple apple;
  private Pear pear;

  private Peanut peanut;
  private Cashew cashew;

  @Inject
  private Orange orange;

  private boolean postConstructFired = false;

  public ConstrInjBean() {
  }

  @Inject
  public ConstrInjBean(final ConstrInjBean constrInjBean, final Apple apple, final Pear pear) {
    this.myself = constrInjBean;
    this.apple = apple;
    this.pear = pear;
  }

  @Inject
  public void setPeanutAndCashew(final Peanut peanut, final Cashew cashew) {
    this.peanut = peanut;
    this.cashew = cashew;
  }


  public ConstrInjBean getMyself() {
    return myself;
  }

  public Apple getApple() {
    return apple;
  }

  public Pear getPear() {
    return pear;
  }

  public Orange getOrange() {
    return orange;
  }

  @PostConstruct
  private void aPostConstructMethod() {
    postConstructFired = true;
  }

  public boolean isPostConstructFired() {
    return postConstructFired;
  }

  public Peanut getPeanut() {
    return peanut;
  }

  public Cashew getCashew() {
    return cashew;
  }
}
