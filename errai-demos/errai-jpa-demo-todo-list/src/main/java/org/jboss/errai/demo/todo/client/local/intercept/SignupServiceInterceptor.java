/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
