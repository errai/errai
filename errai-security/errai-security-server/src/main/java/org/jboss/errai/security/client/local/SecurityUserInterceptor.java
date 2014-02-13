package org.jboss.errai.security.client.local;

import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;

public class SecurityUserInterceptor implements RemoteCallInterceptor<RemoteCallContext> {

  @Override
  public void aroundInvoke(RemoteCallContext context) {
    throw new UnsupportedOperationException("This is a dummy implementation allowing server-side code to compile.");
  }

}
