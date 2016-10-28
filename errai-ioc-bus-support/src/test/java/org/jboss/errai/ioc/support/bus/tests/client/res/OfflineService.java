package org.jboss.errai.ioc.support.bus.tests.client.res;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface OfflineService {

  String greeting(String value);

  String otherGreeting(String value) throws CheckedException;
}
