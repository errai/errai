package org.jboss.errai.security.client.local.nav;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.client.local.context.SecurityContext;

@EntryPoint
public class SecurityContextHoldingSingleton {
  
  private static SecurityContextHoldingSingleton instance;
  
  private final SecurityContext securityContext;
  
  @Inject
  public SecurityContextHoldingSingleton(final SecurityContext securityContext) {
    this.securityContext = securityContext;
  }
  
  @PostConstruct
  private void setInstance() {
    instance = this;
  }
  
  public static SecurityContext getSecurityContext() {
    return instance.securityContext;
  }

}
