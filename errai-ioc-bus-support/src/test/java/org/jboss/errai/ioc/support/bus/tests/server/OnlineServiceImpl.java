package org.jboss.errai.ioc.support.bus.tests.server;

import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.support.bus.tests.client.res.Greeter;
import org.jboss.errai.ioc.support.bus.tests.client.res.OnlineService;

@Service
public class OnlineServiceImpl implements OnlineService {

  @Inject
  private Greeter greeter;

  @Override
  public String greeting(final String value) {
    return greeter.online();
  }

}
