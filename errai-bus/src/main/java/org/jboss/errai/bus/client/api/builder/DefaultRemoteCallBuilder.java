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

package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.framework.ProxyFactory;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.common.client.framework.RpcStub;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Facilitates the building of a remote call. Ensures that the remote call is constructed properly.
 * <p/>
 * Part of the fluent API centered around {@link MessageBuilder}.
 */
public class DefaultRemoteCallBuilder {
  private static Logger logger = LoggerFactory.getLogger(DefaultRemoteCallBuilder.class);
  private static ProxyFactory proxyFactory = new RemoteServiceProxyFactory();

  /* Used to generate a unique number */
  private volatile static int callCounter = 0;

  private final Message message;
  private RemoteCallback<Object> remoteCallback;

  /* The type of response that is expected by the callback */
  private Class<Object> responseType = Object.class;

  public DefaultRemoteCallBuilder(final Message message) {
    this.message = message;
  }

  public <T, R> T call(final RemoteCallback<R> callback, final Class<T> remoteService) {
    return call(callback, null, remoteService);
  }

  public <T, R> T call(final RemoteCallback<R> callback, final BusErrorCallback errorCallback, final Class<T> remoteService) {
    final T svc = proxyFactory.getRemoteProxy(remoteService);
    ((RpcStub) svc).setRemoteCallback(callback);
    ((RpcStub) svc).setErrorCallback(errorCallback);
    return svc;
  }

  /**
   * Only intended for use by generated code. Use {@link #call(RemoteCallback, Class)} or
   * {@link #call(RemoteCallback, BusErrorCallback, Class)} from handwritten code.
   * <p/>
   * Creates, implements and returns an instance of <tt>RemoteCallEndpointDef</tt> and all applicable arguments, which
   * should be instantiated after this call to <tt>serviceName</tt>. The endpoint allows a function from a service to be
   * called directly, rather than waiting for a response to a message.
   *
   * @param serviceName
   *     the service to call, and create a remote call endpoint for
   *
   * @return the remote call endpoint.
   */
  public RemoteCallEndpointDef call(final String serviceName) {
    message.toSubject(serviceName + ":RPC");

    final RemoteCallSendable sendable = bus -> {
      final Integer id;

      final String rpcMethod = message.getCommandType();
      final String replyTo = message.getSubject() + "." + rpcMethod + ":" + (id = uniqueNumber()) + ":RespondTo:RPC";
      final String errorTo = message.getSubject() + "." + rpcMethod + ":" + id + ":Errors:RPC";

      if (remoteCallback != null) {
        bus.subscribe(replyTo, m -> {
          logger.debug("Received RPC response: [service={}, endpoint={}]", serviceName, rpcMethod);
          bus.unsubscribeAll(replyTo);
          if (message.getErrorCallback() != null) {
            bus.unsubscribeAll(errorTo);
          }
          try {
            //Prevent ErraiBus to handle RPC success callback exceptions.
            remoteCallback.callback(m.get(responseType, "MethodReply"));
          } catch (final Exception e) {
            AbstractRpcProxy.DEFAULT_RPC_ERROR_CALLBACK.error(m, new RuntimeException("Client-side exception occurred although RPC call succeeded.", e));
          }
        });
        message.set(MessageParts.ReplyTo, replyTo);
      }

      if (message.getErrorCallback() != null) {
        bus.subscribe(errorTo, m -> {
          logger.debug("Received RPC error: [service={}, endpoint={}]", serviceName, rpcMethod);
          bus.unsubscribeAll(errorTo);
          if (remoteCallback != null) {
            bus.unsubscribeAll(replyTo);
          }
          message.set(MessageParts.AdditionalDetails, m.get(String.class, MessageParts.AdditionalDetails));
          final Throwable throwable = m.get(Throwable.class, MessageParts.Throwable);
          final boolean shouldPerformDefaultErrorHandling = message.getErrorCallback().error(message, throwable);
          if (shouldPerformDefaultErrorHandling) {
            DefaultErrorCallback.INSTANCE.error(message, throwable);
          }
        });
        message.set(MessageParts.ErrorTo, errorTo);
      }

      logger.debug("Sending RPC request: [service={}, endpoint={}]", serviceName, rpcMethod);
      message.sendNowWith(bus);
    };

    final RemoteCallErrorDef errorDef = new RemoteCallErrorDef() {
      @Override
      public RemoteCallSendable errorsHandledBy(@SuppressWarnings("rawtypes") final ErrorCallback errorCallback) {
        if (errorCallback != null) {
          message.errorsCall(errorCallback);
        }
        return sendable;
      }

      @Override
      public RemoteCallSendable defaultErrorHandling() {
        return sendable;
      }
    };

    final RemoteCallResponseDef respondDef = new RemoteCallResponseDef() {
      @Override
      @SuppressWarnings("unchecked")
      public <T> RemoteCallErrorDef respondTo(final Class<T> returnType, final RemoteCallback<T> callback) {
        responseType = (Class<Object>) returnType;
        remoteCallback = (RemoteCallback<Object>) callback;
        return errorDef;
      }
    };

    return new RemoteCallEndpointDef() {
      @Override
      public RemoteCallResponseDef endpoint(final String endPointName) {
        message.command(endPointName);
        return respondDef;
      }

      @Override
      public RemoteCallResponseDef endpoint(final String endPointName, final Annotation[] qualifiers, final Object[] args) {
        message.command(endPointName);

        if (qualifiers != null) {
          final List<String> qualNames = new ArrayList<>(qualifiers.length);
          for (final Annotation a : qualifiers) {
            qualNames.add(a.annotationType().getName());
          }

          message.set("Qualifiers", qualNames);
        }
        if (args != null)
          message.set("MethodParms", Arrays.asList(args));

        return respondDef;
      }

      @Override
      public RemoteCallResponseDef endpoint(final String endPointName, final Object[] args) {
        message.command(endPointName);
        if (args != null)
          message.set("MethodParms", Arrays.asList(args));
        return respondDef;
      }
    };
  }

  private static int uniqueNumber() {
    return ++callCounter > 1000000 ? callCounter = 0 : callCounter;
  }

  /**
   * Sets the proxy provider factory that is used by MessageBuilder and friends for creating remote proxies. Unless you
   * are creating an Errai extension that provides an alternative remoting mechanism, there is never a need to call this
   * method.
   *
   * @param provider
   *     The ProxyProvider that provides RPC proxies to message builders. Not null.
   */
  public static void setProxyFactory(final ProxyFactory provider) {
    proxyFactory = Assert.notNull(provider);
  }

  public static void destroyProxyFactory() {
    proxyFactory = null;
  }
}
