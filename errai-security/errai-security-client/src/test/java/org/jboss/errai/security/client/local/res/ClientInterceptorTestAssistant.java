/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.client.local.res;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.client.local.interceptors.ClientSecurityRoleInterceptor;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

@Dependent
@FeatureInterceptor(RestrictedAccess.class)
public class ClientInterceptorTestAssistant implements RemoteCallInterceptor<RemoteCallContext> {

  private final ClientSecurityRoleInterceptor interceptor;

  public static boolean active = false;

  @Inject
  public ClientInterceptorTestAssistant(final ClientSecurityRoleInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    if (active) {
      interceptCall(context);
    }
    else {
      context.proceed();
    }
  }

  private void interceptCall(final RemoteCallContext context) {
    /*
     * Since we can't guarantee the ordering of interceptors, we will call the interceptor ourselves
     * and then return if successful.
     */
    final RemoteCallContext contextWrapper = new RemoteCallContext() {

      @Override
      public Object proceed() {
        return null;
      }

      @Override
      public Annotation[] getTypeAnnotations() {
        return context.getTypeAnnotations();
      }

      @Override
      public Class getReturnType() {
        return context.getReturnType();
      }

      @Override
      public String getMethodName() {
        return context.getMethodName();
      }

      @Override
      public Annotation[] getAnnotations() {
        return context.getAnnotations();
      }

      @Override
      public void proceed(RemoteCallback<?> callback, ErrorCallback<?> errorCallback) {
      }

      @Override
      public void proceed(RemoteCallback<?> callback) {
      }
    };

    interceptor.aroundInvoke(contextWrapper);
    context.setResult(null);
  }

}
