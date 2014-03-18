package org.jboss.errai.security.client.local.res;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.security.client.shared.SecureRestService;

@ApplicationScoped
public class RestSecurityTestModule {
  
  @Inject
  public Caller<SecureRestService> restCaller;

}
