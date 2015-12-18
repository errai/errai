package org.jboss.errai.ioc.support.bus.tests.client.res;

import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;

import javax.inject.Inject;

public class SimpleSuperBean {
  @Inject
  protected RequestDispatcher dispatcher;
}
