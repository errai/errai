/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.bus.client.api.builder.DefaultRemoteCallBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.RPCEndpointFactory;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.io.ServiceInstanceProvider;
import org.jboss.errai.bus.server.service.bootstrap.BootstrapContext;
import org.jboss.errai.bus.server.service.bootstrap.GuiceProviderProxy;
import org.jboss.errai.bus.server.util.NotAService;
import org.jboss.errai.bus.server.util.ServiceMethodParser;
import org.jboss.errai.bus.server.util.ServiceParser;
import org.jboss.errai.bus.server.util.ServiceTypeParser;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.api.tasks.TaskManager;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.common.client.framework.ProxyFactory;
import org.jboss.errai.common.metadata.MetaDataProcessor;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ServiceProcessor implements MetaDataProcessor<BootstrapContext> {
  private Logger log = LoggerFactory.getLogger(ServiceProcessor.class);

  // TODO need to exclude client classes based on GWT module definition
  private static final String CLIENT_PKG_REGEX = ".*(\\.client\\.).*";

  @Override
  public void process(final BootstrapContext context, MetaDataScanner reflections) {
    final ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl) context.getConfig();
    final Set<Class<?>> services = reflections.getTypesAnnotatedWithExcluding(Service.class, CLIENT_PKG_REGEX);
    final Set<Method> methodServices = reflections.getMethodsAnnotatedWithExcluding(Service.class, CLIENT_PKG_REGEX);

    for (Class<?> loadClass : services) {
      processServiceClass(loadClass, context, config);
    }
    for (Method loadMethod : methodServices) {
      processServiceMethod(loadMethod, context, config);
    }
  }

  private void processServiceClass(final Class<?> loadClass, final BootstrapContext context,
          final ErraiServiceConfiguratorImpl config) {
    ServiceParser svcParser;
    try {
      svcParser = new ServiceTypeParser(loadClass);
    }
    catch (NotAService ex) {
      // Diagnose Errai-111
      StringBuilder sb = new StringBuilder();
      sb.append("Service annotation cannot be loaded. (See https://jira.jboss.org/browse/ERRAI-111)\n");
      sb.append(loadClass.getSimpleName()).append(" loader: ").append(loadClass.getClassLoader()).append("\n");
      sb.append("@Service loader:").append(Service.class.getClassLoader()).append("\n");
      log.warn(sb.toString());
      return;
    }

    Class<?> remoteImpl = ((ServiceTypeParser) svcParser).getRemoteImplementation();
    if (remoteImpl != null) {
      createRPCScaffolding(remoteImpl, loadClass, context);
    }

    processService(svcParser, context);
  }

  private void processServiceMethod(final Method loadMethod, final BootstrapContext context,
          final ErraiServiceConfiguratorImpl config) {
    ServiceParser svcParser;
    try {
      svcParser = new ServiceMethodParser(loadMethod);
    } catch (NotAService ex) {
      // Diagnose Errai-111
      StringBuilder sb = new StringBuilder();
      sb.append("Service annotation cannot be loaded. (See https://jira.jboss.org/browse/ERRAI-111)\n");
      sb.append(loadMethod.getName()).append(" class: ").append(loadMethod.getClass().getSimpleName());
      sb.append(" loader: ").append(loadMethod.getClass().getClassLoader()).append("\n");
      sb.append("@Service loader:").append(Service.class.getClassLoader()).append("\n");
      log.warn(sb.toString());
      return;
    }

    processService(svcParser, context);
  }
  
  private void processService(ServiceParser svcParser, BootstrapContext context) {
    Object svc = null;
    ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl) context.getConfig();
    Class<?> loadClass = svcParser.getDelegateClass();
    
    // Now try and get the appropriate injector
    
    // Will never return true for service methods
    if (svcParser.isCallback()) {
      final Class<? extends MessageCallback> clazz = loadClass.asSubclass(MessageCallback.class);
      log.debug("discovered service: " + clazz.getName());
      try {
        svc = createServiceInjector(clazz, context, config, true);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    if (svc == null) {
      svc = createServiceInjector(loadClass, context, config, false);
    }

    // If we have created an injector, get a callback and register it
    if (svc != null) {
      MessageCallback callback = svcParser.getCallback(svc);
      if (callback != null) {
        if (svcParser.isLocal()) {
          context.getBus().subscribeLocal(svcParser.getServiceName(), callback);
        }
        else {
          context.getBus().subscribe(svcParser.getServiceName(), callback);
        }
      }
    }
  }

  /**
   * Creates an injector for a service. isCallback should be true iff clazz can safely be cast to
   * {@link MessageCallback MessageCallback}.
   */
  private Object createServiceInjector(final Class<?> clazz, final BootstrapContext context,
          final ErraiServiceConfiguratorImpl config, boolean isCallback) {
    Object retVal;
    if (isCallback) {
      retVal = Guice.createInjector(new AbstractModule() {
        @SuppressWarnings("unchecked")
        @Override
        protected void configure() {
          bind(MessageCallback.class).to((Class<? extends MessageCallback>) clazz);
          bind(MessageBus.class).toInstance(context.getBus());
          bind(RequestDispatcher.class).toInstance(context.getService().getDispatcher());
          bind(TaskManager.class).toInstance(TaskManagerFactory.get());

          // Add any extension bindings.
          for (Map.Entry<Class<?>, ResourceProvider> entry : config.getExtensionBindings().entrySet()) {
            bind(entry.getKey()).toProvider(new GuiceProviderProxy(entry.getValue()));
          }
        }
      }).getInstance(MessageCallback.class);
    }
    else {
      retVal = Guice.createInjector(new AbstractModule() {
        @SuppressWarnings("unchecked")
        @Override
        protected void configure() {
          bind(MessageBus.class).toInstance(context.getBus());
          bind(RequestDispatcher.class).toInstance(context.getService().getDispatcher());
          bind(TaskManager.class).toInstance(TaskManagerFactory.get());

          // Add any extension bindings.
          for (Map.Entry<Class<?>, ResourceProvider> entry : config.getExtensionBindings().entrySet()) {
            bind(entry.getKey()).toProvider(new GuiceProviderProxy(entry.getValue()));
          }
        }
      }).getInstance(clazz);
    }

    return retVal;
  }

  private static Class getRemoteImplementation(Class type) {
    for (Class iface : type.getInterfaces()) {
      if (iface.isAnnotationPresent(Remote.class)) {
        return iface;
      }
      else if (iface.getInterfaces().length != 0 && ((iface = getRemoteImplementation(iface)) != null)) {
        return iface;
      }
    }
    return null;
  }

  private static Object createRPCScaffolding(final Class remoteIface, final Class<?> type,
          final BootstrapContext context) {

    final ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl) context.getConfig();
    final Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(MessageBus.class).toInstance(context.getBus());
        bind(RequestDispatcher.class).toInstance(context.getService().getDispatcher());
        bind(TaskManager.class).toInstance(TaskManagerFactory.get());

        // Add any extension bindings.
        for (Map.Entry<Class<?>, ResourceProvider> entry : config.getExtensionBindings().entrySet()) {
          bind(entry.getKey()).toProvider(new GuiceProviderProxy(entry.getValue()));
        }
      }
    });

    final Object svc = injector.getInstance(type);

    final Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();

    final ServiceInstanceProvider genericSvc = new ServiceInstanceProvider() {
      @Override
      public Object get(Message message) {
        return svc;
      }
    };
    // beware of classloading issues. better reflect on the actual instance
    for (Class<?> intf : svc.getClass().getInterfaces()) {
      for (final Method method : intf.getMethods()) {
        if (ProxyUtil.isMethodInInterface(remoteIface, method)) {
          epts.put(ProxyUtil.createCallSignature(intf, method),
                  RPCEndpointFactory.createEndpointFor(genericSvc, method, context.getBus()));
        }
      }
    }

    context.getBus().subscribe(remoteIface.getName() + ":RPC", new RemoteServiceCallback(epts));

    // note: this method just exists because we want AbstractRemoteCallBuilder to be package
    // private.
    DefaultRemoteCallBuilder.setProxyFactory(Assert.notNull(new ProxyFactory() {
      @Override
      public <T> T getRemoteProxy(Class<T> proxyType) {
        throw new RuntimeException(
                "There is not yet an available Errai RPC implementation for the server-side environment.");
      }
    }));

    return svc;
  }
}
