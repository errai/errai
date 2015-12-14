package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;

public class JsTypeConsumer {

  @Inject
  private UnimplementedType iface;

  public UnimplementedType getIface() {
    return iface;
  }

}
