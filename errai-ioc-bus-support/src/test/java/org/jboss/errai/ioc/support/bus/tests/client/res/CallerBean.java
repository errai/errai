package org.jboss.errai.ioc.support.bus.tests.client.res;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.errai.common.client.api.Caller;

@Singleton
public class CallerBean {

  @Inject
  private Caller<OfflineService> offlineService;
  
  @Inject
  private Caller<OnlineService> onlineService;
  
  public Caller<OfflineService> getOfflineServiceCaller() {
    return offlineService;
  }
  
  public Caller<OnlineService> getOnlineServiceCaller() {
    return onlineService;
  }
}
