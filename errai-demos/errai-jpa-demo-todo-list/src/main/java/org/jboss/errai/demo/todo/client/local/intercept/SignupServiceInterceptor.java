package org.jboss.errai.demo.todo.client.local.intercept;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.demo.todo.shared.RegistrationResult;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.security.client.local.util.SecurityUtil;

@InterceptsRemoteCall({ SignupService.class })
public class SignupServiceInterceptor implements RemoteCallInterceptor<RemoteCallContext> {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    context.proceed(new RemoteCallback<RegistrationResult>() {

      @Override
      public void callback(final RegistrationResult response) {
        SecurityUtil.performLoginStatusChangeActions(response.getSecurityUser());
        context.setResult(response);
      }
    });
  }

}
