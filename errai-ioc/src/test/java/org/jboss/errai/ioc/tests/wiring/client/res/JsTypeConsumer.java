package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;

public class JsTypeConsumer {

  @Inject
  private JsTypeUnimplemented iface;

  public JsTypeUnimplemented getIface() {
    return iface;
  }

}
