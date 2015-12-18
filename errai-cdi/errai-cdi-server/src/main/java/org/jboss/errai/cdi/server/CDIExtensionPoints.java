/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server;

import static java.util.ResourceBundle.getBundle;
import static org.jboss.errai.cdi.server.CDIServerUtil.lookupRPCBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.inject.Qualifier;

import org.jboss.errai.bus.client.api.builder.DefaultRemoteCallBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.AsyncDispatcher;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.SimpleDispatcher;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.RPCEndpointFactory;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.io.ServiceInstanceProvider;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.bus.server.util.NotAService;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.bus.server.util.ServiceMethodParser;
import org.jboss.errai.bus.server.util.ServiceParser;
import org.jboss.errai.bus.server.util.ServiceTypeParser;
import org.jboss.errai.cdi.server.events.AnyEventObserver;
import org.jboss.errai.cdi.server.events.EventDispatcher;
import org.jboss.errai.cdi.server.events.EventRoutingTable;
import org.jboss.errai.cdi.server.events.ShutdownEventObserver;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.framework.ProxyFactory;
import org.jboss.errai.common.server.api.ErraiBootstrapFailure;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension points to the CDI container. Makes Errai components available as CDI beans (i.e. the
 * message bus) and registers CDI components as services with Errai.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class CDIExtensionPoints implements Extension {
  private static final Logger log = LoggerFactory.getLogger(CDIExtensionPoints.class);

  private final TypeRegistry managedTypes = new TypeRegistry();

  private final Set<MessageSender> messageSenders = new LinkedHashSet<MessageSender>();
  private final Map<String, Annotation> eventQualifiers = new HashMap<String, Annotation>();
  private final Map<String, Annotation> beanQualifiers = new HashMap<String, Annotation>();

  private final Set<String> observableEvents = new HashSet<String>();

  private static final Set<String> vetoClasses;

  private static final String ERRAI_CDI_STANDALONE = "errai.cdi.standalone";

  static {
    final Set<String> veto = new HashSet<String>();
    veto.add(ServerMessageBusImpl.class.getName());
    veto.add(RequestDispatcher.class.getName());
    veto.add(ErraiService.class.getName());

    vetoClasses = Collections.unmodifiableSet(veto);
  }

  public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd) {
    log.info("starting errai cdi ...");
    final ResourceBundle erraiServiceConfig;
    try {
      erraiServiceConfig = getBundle("ErraiService");
    } catch (MissingResourceException e) {
      // ErraiService is optional!
      return;
    }

    if (erraiServiceConfig.containsKey(ERRAI_CDI_STANDALONE)) {
      final boolean standalone = "true".equals(erraiServiceConfig.getString(ERRAI_CDI_STANDALONE).trim());

      if (standalone) {
        log.info("errai cdi running in standalone mode.");
      }
      else {
        log.info("errai cdi running as regular extension.");
      }
    }

    final String dispatchImplKey = "errai.dispatcher_implementation";
    if (erraiServiceConfig.containsKey(dispatchImplKey)) {
      if (AsyncDispatcher.class.getName().equals(erraiServiceConfig.getString(dispatchImplKey))) {
        throw new ErraiBootstrapFailure("Cannot start Errai CDI. You have have configured the service to use the "
                + AsyncDispatcher.class.getName()
                + " dispatcher implementation. Due to limitations of Weld, you must use the "
                + SimpleDispatcher.class.getName() + " in order to use this module.");
      }
    }
  }

  /**
   * Register managed beans as Errai services
   *
   * @param event
   *          -
   * @param <T>
   *          -
   */
  @SuppressWarnings("rawtypes")
  public <T> void observeResources(@Observes final ProcessAnnotatedType<T> event) {
    final AnnotatedType<T> type = event.getAnnotatedType();

    for (final Annotation a : type.getJavaClass().getAnnotations()) {
      if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
        beanQualifiers.put(a.annotationType().getName(), a);
      }
    }

    // services
    if (type.isAnnotationPresent(Service.class)) {
      log.info("discovered errai service: " + type);
      boolean isRpc = false;

      final Class<T> javaClass = type.getJavaClass();
      for (final Class<?> intf : javaClass.getInterfaces()) {
        isRpc = intf.isAnnotationPresent(Remote.class);

        if (isRpc) {
          if (!managedTypes.getRemoteInterfaces().contains(intf)) {
            managedTypes.addRemoteInterface(intf);
          }
        }
      }

      if (!isRpc) {
        try {
          managedTypes.addService(new ServiceTypeParser(type.getJavaClass()));
        } catch (NotAService e) {
          e.printStackTrace();
        }
      }
    }
    for (final AnnotatedMethod method : type.getMethods()) {
      if (method.isAnnotationPresent(Service.class)) {
        try {
          managedTypes.addService(new ServiceMethodParser(method.getJavaMember()));
        } catch (NotAService e) {
          e.printStackTrace();
        }
      }
    }

    // veto on client side implementations that contain CDI annotations
    // (i.e. @Observes) Otherwise Weld might try to invoke on them
    Class<?> javaClass = type.getJavaClass();
    Package pkg = javaClass.getPackage();
    if (vetoClasses.contains(javaClass.getName())
            || (pkg != null && pkg.getName().matches("(^|.*\\.)client(?!\\.shared)(\\..*)?")
            && !javaClass.isInterface())) {
      log.debug("Vetoing processed type: " + javaClass.getName());
      event.veto();
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void processObserverMethod(@Observes final ProcessObserverMethod processObserverMethod) {
    final Type t = processObserverMethod.getObserverMethod().getObservedType();
    Class type = null;

    if (t instanceof Class) {
      type = (Class) t;
    }

    ClassScanner.setReflectionsScanning(true);

    if (type != null && EnvUtil.isPortableType(type) && !EnvUtil.isLocalEventType(type)) {
      final Set<Annotation> annotations = processObserverMethod.getObserverMethod().getObservedQualifiers();
      final Annotation[] methodQualifiers = annotations.toArray(new Annotation[annotations.size()]);
      for (final Annotation qualifier : methodQualifiers) {
        eventQualifiers.put(qualifier.annotationType().getName(), qualifier);
      }

      observableEvents.add(type.getName());
    }
  }

  @SuppressWarnings("rawtypes")
  public void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd, final BeanManager bm) {
    final ErraiService service = ErraiServiceSingleton.getService();
    final MessageBus bus = service.getBus();
    final EventRoutingTable eventRoutingTable = new EventRoutingTable();

    if (bus.isSubscribed(CDI.SERVER_DISPATCHER_SUBJECT)) {
      return;
    }

    final byte[] randBytes = new byte[32];
    final Random random = new Random(System.currentTimeMillis());
    random.nextBytes(randBytes);

    abd.addBean(new ErraiServiceBean(bm, SecureHashUtil.hashToHexString(randBytes)));

    for (final MessageSender ms : messageSenders) {
      abd.addBean(new SenderBean(ms.getSenderType(), ms.getQualifiers(), bus));
    }

    // Errai bus injection
    abd.addBean(new MessageBusBean(bus));

    // Support to inject the request dispatcher.
    abd.addBean(new RequestDispatcherMetaData(bm, service.getDispatcher()));

    // Register observers
    abd.addObserverMethod(new ShutdownEventObserver(managedTypes, bus));

    // subscribe service and rpc endpoints
    subscribeServices(bm, bus);

    // initialize the CDI event bridge to the client
    final EventDispatcher eventDispatcher =
            new EventDispatcher(bm, eventRoutingTable, bus, observableEvents, eventQualifiers);

    AnyEventObserver.init(eventDispatcher);

    // subscribe event dispatcher
    bus.subscribe(CDI.SERVER_DISPATCHER_SUBJECT, eventDispatcher);
  }

  /**
   * Registers beans (type and method services) as they become available from the bean manager.
   */
  private class StartupCallback implements Runnable {
    private final Set<Object> toRegister = new HashSet<Object>();
    private final BeanManager beanManager;
    private final MessageBus bus;
    private final ScheduledExecutorService scheduledExecutorService;
    private final long expiryTime;

    private StartupCallback(final BeanManager beanManager, final MessageBus bus,
            final ScheduledExecutorService scheduledExecutorService, final int timeOutInSeconds) {
      this.beanManager = beanManager;
      this.bus = bus;
      this.scheduledExecutorService = scheduledExecutorService;
      toRegister.addAll(managedTypes.getDelegateClasses());

      this.expiryTime = System.currentTimeMillis() + (timeOutInSeconds * 1000);
    }

    private Annotation[] getQualifiers(Class<?> delegateClass) {
      int length = 0;
      for (Annotation anno : delegateClass.getAnnotations()) {
        if (anno.annotationType().isAnnotationPresent(Qualifier.class))
          length += 1;
      }

      Annotation[] ret = new Annotation[length];
      int i = 0;
      for (Annotation anno : delegateClass.getAnnotations()) {
        if (anno.annotationType().isAnnotationPresent(Qualifier.class))
          ret[i++] = anno;
      }

      return ret;
    }

    @Override
    public void run() {
      if (System.currentTimeMillis() > expiryTime) {
        scheduledExecutorService.shutdown();
        throw new RuntimeException("failed to discover beans: " + managedTypes.getDelegateClasses());
      }

      if (toRegister.isEmpty()) {
        scheduledExecutorService.shutdown();
        return;
      }

      // As each delegate becomes available, register all the associated services (type and method)
      for (final Class<?> delegateClass : managedTypes.getDelegateClasses()) {
        try {
          if (!toRegister.contains(delegateClass) || beanManager.getBeans(delegateClass, getQualifiers(delegateClass)).size() == 0) {
            continue;
          }
        }
        catch(Throwable t) {
          continue;
        }

        for (final ServiceParser svcParser : managedTypes.getDelegateServices(delegateClass)) {
          final Object delegateInstance;
          try {
            delegateInstance = CDIServerUtil.lookupBean(beanManager, delegateClass, getQualifiers(delegateClass));
          }
          catch (IllegalStateException t) {
            // handle WELD-001332: BeanManager method getReference() is not available during application initialization
            // try again later...
            return;
          }
          final MessageCallback callback = svcParser.getCallback(delegateInstance);
          if (callback != null) {
            if (svcParser.isLocal()) {
              bus.subscribeLocal(svcParser.getServiceName(), callback);
            }
            else {
              bus.subscribe(svcParser.getServiceName(), callback);
            }
          }
        }
        toRegister.remove(delegateClass);
      }
    }
  }

  private void subscribeServices(final BeanManager beanManager, final MessageBus bus) {
    /**
     * Due to the lack of contract in CDI guaranteeing when beans will be available, we use an
     * executor to search for the beans every 100ms until it finds them. Or, after a 25 seconds,
     * blow up if they don't become available.
     */
    final ScheduledExecutorService startupScheduler = Executors.newScheduledThreadPool(1);
    startupScheduler.scheduleAtFixedRate(new StartupCallback(beanManager, bus, startupScheduler, 25), 0, 100,
            TimeUnit.MILLISECONDS);

    for (final Class<?> remoteInterfaceType : managedTypes.getRemoteInterfaces()) {
      createRPCScaffolding(remoteInterfaceType, bus, beanManager);
    }
  }

  private void createRPCScaffolding(final Class<?> remoteIface, final MessageBus bus, final BeanManager beanManager) {
    final Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();

    final ServiceInstanceProvider genericSvc = new ServiceInstanceProvider() {
      @SuppressWarnings("unchecked")
      @Override
      public Object get(final Message message) {
        if (message.hasPart(CDIProtocol.Qualifiers)) {
          final List<String> quals = message.get(List.class, CDIProtocol.Qualifiers);
          final Annotation[] qualAnnos = new Annotation[quals.size()];
          for (int i = 0; i < quals.size(); i++) {
            qualAnnos[i] = beanQualifiers.get(quals.get(i));
          }
          return lookupRPCBean(beanManager, remoteIface, qualAnnos);
        }
        else {
          return lookupRPCBean(beanManager, remoteIface, null);
        }
      }
    };
    // beware of classloading issues. better reflect on the actual instance
    for (final Method method : remoteIface.getMethods()) {
      if (ProxyUtil.isMethodInInterface(remoteIface, method)) {

        epts.put(ProxyUtil.createCallSignature(remoteIface, method),
                RPCEndpointFactory.createEndpointFor(genericSvc, method, bus));
      }
    }

    final RemoteServiceCallback delegate = new RemoteServiceCallback(epts);
    bus.subscribe(remoteIface.getName() + ":RPC", new MessageCallback() {
      @Override
      public void callback(final Message message) {
        delegate.callback(message);
      }
    });

    log.debug("registered RPC service for: " + remoteIface.getName());

    // note: this method just exists because we want AbstractRemoteCallBuilder to be package
    // private.
    DefaultRemoteCallBuilder.setProxyFactory(Assert.notNull(new ProxyFactory() {
      @Override
      public <T> T getRemoteProxy(final Class<T> proxyType) {
        throw new RuntimeException(
                "There is not yet an available Errai RPC implementation for the server-side environment.");
      }
    }));
  }

  static class MessageSender {
    private final Type senderType;
    private final Set<Annotation> qualifiers;

    MessageSender(final Type senderType, final Set<Annotation> qualifiers) {
      this.senderType = senderType;
      this.qualifiers = qualifiers;
    }

    public Type getSenderType() {
      return senderType;
    }

    public Set<Annotation> getQualifiers() {
      return qualifiers;
    }
  }
}
