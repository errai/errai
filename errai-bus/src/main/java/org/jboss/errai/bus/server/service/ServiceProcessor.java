/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.bus.server.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.api.annotations.Local;
import org.jboss.errai.bus.client.api.base.TaskManagerFactory;
import org.jboss.errai.bus.client.api.builder.AbstractRemoteCallBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.ProxyProvider;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.rebind.RebindUtils;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Endpoint;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.io.CommandBindingsCallback;
import org.jboss.errai.bus.server.io.ConversationalEndpointCallback;
import org.jboss.errai.bus.server.io.EndpointCallback;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.security.auth.rules.RolesRequiredRule;
import org.jboss.errai.bus.server.service.bootstrap.BootstrapContext;
import org.jboss.errai.bus.server.service.bootstrap.GuiceProviderProxy;
import org.jboss.errai.common.metadata.MetaDataProcessor;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 3, 2010
 */
public class ServiceProcessor implements MetaDataProcessor<BootstrapContext> {
  private Logger log = LoggerFactory.getLogger(ServiceProcessor.class);

  public void process(final BootstrapContext context, MetaDataScanner reflections) {
    final ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl) context.getConfig();
    final Set<Class<?>> services = reflections.getTypesAnnotatedWithExcluding(Service.class, MetaDataScanner.CLIENT_PKG_REGEX);

    for (Class<?> loadClass : services) {
      Object svc = null;

      Service svcAnnotation = loadClass.getAnnotation(Service.class);
      if (null == svcAnnotation) {
        // Diagnose Errai-111
        StringBuffer sb = new StringBuffer();
        sb.append("Service annotation cannot be loaded. (See https://jira.jboss.org/browse/ERRAI-111)\n");
        sb.append(loadClass.getSimpleName()).append(" loader: ").append(loadClass.getClassLoader()).append("\n");
        sb.append("@Service loader:").append(Service.class.getClassLoader()).append("\n");
        log.warn(sb.toString());
        continue;
      }

      boolean local = loadClass.isAnnotationPresent(Local.class);

      String svcName = svcAnnotation.value();

      // If no name is specified, just use the class name as the service by default.
      if ("".equals(svcName)) {
        svcName = loadClass.getSimpleName();
      }

      Map<String, Method> commandPoints = new HashMap<String, Method>();
      for (final Method method : loadClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Command.class)) {
          Command command = method.getAnnotation(Command.class);
          for (String cmdName : command.value()) {
            if (cmdName.equals("")) cmdName = method.getName();
            commandPoints.put(cmdName, method);
          }
        }
      }

      Class remoteImpl = getRemoteImplementation(loadClass);
      if (remoteImpl != null) {
        svc = createRPCScaffolding(remoteImpl, loadClass, context);
      }
      else if (MessageCallback.class.isAssignableFrom(loadClass)) {
        final Class<? extends MessageCallback> clazz = loadClass.asSubclass(MessageCallback.class);
        log.info("discovered service: " + clazz.getName());
        try {
          svc = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
              bind(MessageCallback.class).to(clazz);
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
        catch (Throwable t) {
          t.printStackTrace();
        }

        if (commandPoints.isEmpty()) {
          // Subscribe the service to the bus.
          context.getBus().subscribe(svcName, (MessageCallback) svc);
        }

        RolesRequiredRule rule = null;
        if (clazz.isAnnotationPresent(RequireRoles.class)) {
          rule = new RolesRequiredRule(clazz.getAnnotation(RequireRoles.class).value(), context.getBus());
        }
        else if (clazz.isAnnotationPresent(RequireAuthentication.class)) {
          rule = new RolesRequiredRule(new HashSet<Object>(), context.getBus());
        }
        if (rule != null) {
          context.getBus().addRule(svcName, rule);
        }
      }

      if (svc == null) {
        svc = Guice.createInjector(new AbstractModule() {
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
        }).getInstance(loadClass);
      }

      Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();

      final Object targetService = svc;
      
      // we scan for endpoints
      for (final Method method : loadClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Endpoint.class)) {
          epts.put(method.getName(), method.getReturnType() == Void.class ?
              new EndpointCallback(svc, method) :
              new ConversationalEndpointCallback(new Provider<Object>() {
                @Override
                public Object get() {
                  return targetService;
                }
              }, method, context.getBus()));
        }
      }

      if (!epts.isEmpty()) {
        if (local) {
          context.getBus().subscribeLocal(loadClass.getSimpleName() + ":RPC", new RemoteServiceCallback(epts));
        }
        else {
          context.getBus().subscribe(loadClass.getSimpleName() + ":RPC", new RemoteServiceCallback(epts));
        }
      }

      if (!commandPoints.isEmpty()) {
        if (local) {
          context.getBus().subscribeLocal(svcName, new CommandBindingsCallback(commandPoints, svc));

        }
        else {
          context.getBus().subscribe(svcName, new CommandBindingsCallback(commandPoints, svc));
        }
      }
    }
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

  private static Object createRPCScaffolding(final Class remoteIface, final Class<?> type, final BootstrapContext context) {

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

    // beware of classloading issues. better reflect on the actual instance
    for (Class<?> intf : svc.getClass().getInterfaces()) {
      for (final Method method : intf.getDeclaredMethods()) {
        if (RebindUtils.isMethodInInterface(remoteIface, method)) {
          epts.put(RebindUtils.createCallSignature(method), new ConversationalEndpointCallback(new Provider<Object>() {
            @Override
            public Object get() {
              return svc;
            }
          }, method, context.getBus()));
        }
      }
    }

    context.getBus().subscribe(remoteIface.getName() + ":RPC", new RemoteServiceCallback(epts));

    new ProxyProvider() {
      {
        AbstractRemoteCallBuilder.setProxyFactory(this);
      }

      public <T> T getRemoteProxy(Class<T> proxyType) {
        throw new RuntimeException("This API is not supported in the server-side environment.");
      }
    };
    
    return svc;
  }
}
