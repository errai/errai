package org.jboss.errai.ioc.support.bus.tests.client.res;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface OnlineService {

  String greeting(String value);
}
