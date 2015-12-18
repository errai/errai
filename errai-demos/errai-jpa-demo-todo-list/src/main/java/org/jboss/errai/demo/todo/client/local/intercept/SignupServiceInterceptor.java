package org.jboss.errai.demo.todo.client.local.intercept;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.TodoListUser;
import org.jboss.errai.security.client.local.api.SecurityContext;

@InterceptsRemoteCall({ SignupService.class })
@Dependent
public class SignupServiceInterceptor implements RemoteCallInterceptor<RemoteCallContext> {
  
  @Inject
  private SecurityContext securityContext;

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    context.proceed(new RemoteCallback<TodoListUser>() {

      @Override
      public void callback(final TodoListUser user) {
        securityContext.setCachedUser(user);
        context.setResult(user);
      }
    });
  }

}
