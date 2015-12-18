/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.client.api.builtin;


import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.NoOpCallback;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.common.client.framework.RpcStub;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
@IOCProvider @Singleton
@SuppressWarnings("rawtypes")
public class CallerProvider implements ContextualTypeProvider<Caller> {
  private static final RemoteServiceProxyFactory factory = new RemoteServiceProxyFactory();

  @Override
  public Caller provide(final Class<?>[] typeargs, final Annotation[] qualifiers) {
    return new Caller<Object>() {
      @Override
      public Object call() {
        final Object proxy = factory.getRemoteProxy(typeargs[0]);
        ((RpcStub) proxy).setRemoteCallback(new NoOpCallback());
        ((RpcStub) proxy).setQualifiers(qualifiers);
        return proxy;
      }

      @Override
      public Object call(final RemoteCallback<?> callback) {
        final Object proxy = factory.getRemoteProxy(typeargs[0]);
        ((RpcStub) proxy).setRemoteCallback(callback);
        ((RpcStub) proxy).setQualifiers(qualifiers);
        return proxy;
      }

      @Override
      public Object call(final RemoteCallback<?> callback, final ErrorCallback<?> errorCallback) {
        final Object proxy = factory.getRemoteProxy(typeargs[0]);
        ((RpcStub) proxy).setRemoteCallback(callback);
        ((RpcStub) proxy).setErrorCallback(errorCallback);
        ((RpcStub) proxy).setQualifiers(qualifiers);
        return proxy;
      }
    };
  }
}
