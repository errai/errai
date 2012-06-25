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
package org.jboss.errai.cdi.server;


import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.builder.DefaultRemoteCallBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.ProxyFactory;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.util.ErrorHelper;
import org.jboss.errai.bus.rebind.RebindUtils;
import org.jboss.errai.bus.server.AsyncDispatcher;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.SimpleDispatcher;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.CommandBindingsCallback;
import org.jboss.errai.bus.server.io.ConversationalEndpointCallback;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.io.ServiceInstanceProvider;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.cdi.server.events.ConversationalEvent;
import org.jboss.errai.cdi.server.events.ConversationalEventBean;
import org.jboss.errai.cdi.server.events.ConversationalEventObserverMethod;
import org.jboss.errai.cdi.server.events.EventDispatcher;
import org.jboss.errai.cdi.server.events.EventObserverMethod;
import org.jboss.errai.cdi.server.events.ShutdownEventObserver;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.common.rebind.EnvUtil;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;
import org.jboss.errai.enterprise.client.cdi.internal.ObserverModel;
import org.jboss.errai.enterprise.rebind.ObserversMarshallingExtension;
import org.jboss.errai.ioc.client.api.Sender;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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

import static java.util.ResourceBundle.getBundle;
import static org.jboss.errai.cdi.server.CDIServerUtil.lookupRPCBean;

/**
 * Extension points to the CDI container. Makes Errai components available as CDI beans (i.e. the message bus) and
 * registers CDI components as services with Errai.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class CDIExtensionPoints implements Extension {
  private static final Logger log = LoggerFactory.getLogger(CDIExtensionPoints.class);

  private final TypeRegistry managedTypes = new TypeRegistry();

  private final Set<EventConsumer> eventConsumers = new LinkedHashSet<EventConsumer>();
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

  @SuppressWarnings("UnusedDeclaration")
  public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd) {
    log.info("starting errai cdi ...");
    final ResourceBundle erraiServiceConfig;
    try {
      erraiServiceConfig = getBundle("ErraiService");
    }
    catch (MissingResourceException e) {
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
        throw new ErraiBootstrapFailure("Cannot start Errai CDI. You have have configured the service to use the " +
                AsyncDispatcher.class.getName()
                + " dispatcher implementation. Due to limitations of Weld, you must use the " +
                SimpleDispatcher.class.getName() + " in order to use this module.");
      }
    }
  }

  /**
   * Register managed beans as Errai services
   *
   * @param event
   *         -
   * @param <T>
   *         -
   */
  @SuppressWarnings("UnusedDeclaration")
  public <T> void observeResources(@Observes final ProcessAnnotatedType<T> event) {
    final AnnotatedType<T> type = event.getAnnotatedType();

    for (final Annotation a : type.getJavaClass().getAnnotations()) {
      if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
        beanQualifiers.put(a.annotationType().getName(), a);
      }
    }

    // services
    if (type.isAnnotationPresent(Service.class)) {
      log.debug("discovered Errai annotation on type: " + type);
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
        managedTypes.addServiceEndpoint(type);
      }
    }
    else {
      for (final AnnotatedMethod method : type.getMethods()) {
        if (method.isAnnotationPresent(Service.class)) {
          managedTypes.addServiceMethod(type, method);
        }
      }
    }

    // veto on client side implementations that contain CDI annotations
    // (i.e. @Observes) Otherwise Weld might try to invoke on them
    if (vetoClasses.contains(type.getJavaClass().getName())
            || (type.getJavaClass().getPackage().getName().contains("client") && !type.getJavaClass().isInterface())) {
      event.veto();
    }
    /**
     * We must scan for Event consumer injection points to build the tables
     */
    final Class clazz = type.getJavaClass();

    for (final Field f : clazz.getDeclaredFields()) {
      if (f.isAnnotationPresent(Inject.class) && f.isAnnotationPresent(ObserverModel.class)) {
        processEventInjector(f.getType(), f.getGenericType(), f.getAnnotations());
      }
    }
    for (final Method m : clazz.getDeclaredMethods()) {
      if (m.isAnnotationPresent(Inject.class) && m.isAnnotationPresent(ObserverModel.class)) {
        final Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
          final Class<?> parmType = parameterTypes[i];
          processEventInjector(parmType, m.getGenericParameterTypes()[i], m.getParameterAnnotations()[i]);
        }
      }
    }
    for (final Constructor c : clazz.getDeclaredConstructors()) {
      if (c.isAnnotationPresent(Inject.class) && c.isAnnotationPresent(ObserverModel.class)) {
        final Class<?>[] parameterTypes = c.getParameterTypes();
        for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
          final Class<?> parmType = parameterTypes[i];
          processEventInjector(parmType, c.getGenericParameterTypes()[i], c.getParameterAnnotations()[i]);
        }
      }
    }
  }

  private void processEventInjector(final Class<?> type, final Type typeParm, final Annotation[] annotations) {
    if (Event.class.isAssignableFrom(type)) {
      final ParameterizedType pType = (ParameterizedType) typeParm;
      final Class eventType = (Class) pType.getActualTypeArguments()[0];

      for (final Class<?> observesType : EnvUtil.getAllPortableSubtypes(eventType)) {
        final List<Annotation> qualifiers = new ArrayList<Annotation>();

        /**
         * Collect Qualifier types for the Event consumer.
         */
        for (final Annotation annotation : annotations) {
          if (annotation.annotationType().isAnnotationPresent(Qualifier.class)
                  && !annotation.annotationType().equals(ObserverModel.class)) {
            qualifiers.add(annotation);
            eventQualifiers.put(annotation.annotationType().getName(), annotation);
          }
        }

        eventConsumers.add(new EventConsumer(observesType.isAnnotationPresent(Conversational.class),
                null, observesType, qualifiers.toArray(new Annotation[qualifiers.size()])));
      }
    }
    else if (ConversationalEvent.class.isAssignableFrom(type)) {
      final ParameterizedType pType = (ParameterizedType) typeParm;

      final Class eventType = (Class) pType.getActualTypeArguments()[0];

      for (final Class<?> observesType : EnvUtil.getAllPortableSubtypes(eventType)) {
        final List<Annotation> qualifiers = new ArrayList<Annotation>();

        /**
         * Collect Qualifier types for the Event consumer.
         */
        for (final Annotation annotation : annotations) {
          if (annotation.annotationType().isAnnotationPresent(Qualifier.class)
                  && !annotation.annotationType().equals(ObserverModel.class)) {
            qualifiers.add(annotation);
            eventQualifiers.put(annotation.annotationType().getName(), annotation);
          }
        }
        eventConsumers.add(new EventConsumer(true, typeParm, observesType,
                qualifiers.toArray(new Annotation[qualifiers.size()])));
      }
    }
    else if (Sender.class.isAssignableFrom(type)) {
      final ParameterizedType pType = (ParameterizedType) typeParm;
      final Class sendType = (Class) pType.getActualTypeArguments()[0];
      final Set<Annotation> qualifiers = new HashSet<Annotation>();

      /**
       * Collect Qualifier types for the Event consumer.
       */
      for (final Annotation annotation : annotations) {
        if (annotation.annotationType().isAnnotationPresent(Qualifier.class)
                && !annotation.annotationType().equals(ObserverModel.class)) {
          qualifiers.add(annotation);
          eventQualifiers.put(annotation.annotationType().getName(), annotation);
        }
      }

      if (EnvUtil.isPortableType(sendType)) {
        messageSenders.add(new MessageSender(typeParm, qualifiers));
      }
    }
  }

  @SuppressWarnings({"UnusedDeclaration", "unchecked"})
  public void processObserverMethod(@Observes final ProcessObserverMethod processObserverMethod) {
    final Type t = processObserverMethod.getObserverMethod().getObservedType();
    Class type = null;

    if (t instanceof Class) {
      type = (Class) t;
    }

    if (type != null && EnvUtil.isPortableType(type)) {
      final Set<Annotation> annotations = processObserverMethod.getObserverMethod().getObservedQualifiers();
      final Annotation[] methodQualifiers = annotations.toArray(new Annotation[annotations.size()]);
      for (final Annotation qualifier : methodQualifiers) {
        eventQualifiers.put(qualifier.annotationType().getName(), qualifier);
      }

      observableEvents.add(type.getName());
    }
  }

  @SuppressWarnings({"UnusedDeclaration", "CdiInjectionPointsInspection"})
  public void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd, final BeanManager bm) {
    final ErraiServiceSingleton.ErraiInitCallback callback = new ErraiServiceSingleton.ErraiInitCallback() {
      @Override
      public void onInit(final ErraiService service) {
        // subscribe event dispatcher
        // Errai Service wrapper

        final MessageBus bus = service.getBus();

        if (bus.isSubscribed(CDI.SERVER_DISPATCHER_SUBJECT)) {
          return;
        }

        final byte[] randBytes = new byte[32];
        final Random random = new Random(System.currentTimeMillis());

        random.nextBytes(randBytes);

        abd.addBean(new ErraiServiceBean(bm, SecureHashUtil.hashToHexString(randBytes)));

        if (MarshallingGenUtil.isUseStaticMarshallers()) {

          final Set<ObserversMarshallingExtension.ObserverPoint> observerPoints
                  = new HashSet<ObserversMarshallingExtension.ObserverPoint>();

          for (final EventConsumer ec : eventConsumers) {
            if (ec.getEventBeanType() != null) {
              abd.addBean(new ConversationalEventBean(ec.getEventBeanType(), (BeanManagerImpl) bm, bus));
            }

            observerPoints.add(new ObserversMarshallingExtension.ObserverPoint(ec.getRawType(), ec.getQualifiers()));
          }

          observerPoints.addAll(org.jboss.errai.enterprise.rebind.ObserversMarshallingExtension.scanForObserverPointsInClassPath());

          for (final org.jboss.errai.enterprise.rebind.ObserversMarshallingExtension.ObserverPoint observerPoint :
                  observerPoints) {
            if (org.jboss.errai.common.rebind.EnvUtil.isPortableType(observerPoint.getObservedType())) {
              if (observerPoint.getObservedType().isAnnotationPresent(Conversational.class)) {
                abd.addObserverMethod(new ConversationalEventObserverMethod(observerPoint.getObservedType(), bus, observerPoint.getQualifiers()));
              }
              else {
                abd.addObserverMethod(new EventObserverMethod(observerPoint.getObservedType(), bus, observerPoint.getQualifiers()));
              }
            }
          }
        }
//        else {
//          bus.subscribe("cdi.event:DevModeService", new MessageCallback() {
//            @Override
//            public void callback(final Message message) {
//              CDIProtocol.valueOf()
//            }
//          });
//        }

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

        final EventDispatcher eventDispatcher
                = new EventDispatcher(bm, bus, observableEvents,
                eventQualifiers, MarshallingGenUtil.isUseStaticMarshallers() ? null : abd);

        // subscribe event dispatcher
        bus.subscribe(CDI.SERVER_DISPATCHER_SUBJECT, eventDispatcher);
      }
    };
    ErraiServiceSingleton.registerInitCallback(callback);

  }

  private class StartupCallback implements Runnable {
    private final Set<Object> registered = new HashSet<Object>();
    private final BeanManager beanManager;
    private final MessageBus bus;
    private final ScheduledExecutorService scheduledExecutorService;
    private final long expiryTime;

    private StartupCallback(final BeanManager beanManager,
                            final MessageBus bus,
                            final ScheduledExecutorService scheduledExecutorService,
                            final int timeOutInSeconds) {
      this.beanManager = beanManager;
      this.bus = bus;
      this.scheduledExecutorService = scheduledExecutorService;
      registered.addAll(managedTypes.getServiceEndpoints());

      this.expiryTime = System.currentTimeMillis() + (timeOutInSeconds * 1000);
    }

    @Override
    public void run() {
      if (System.currentTimeMillis() > expiryTime) {
        scheduledExecutorService.shutdown();
        throw new RuntimeException("failed to discover beans: " + managedTypes.getServiceEndpoints());
      }

      if (registered.isEmpty()) {
        scheduledExecutorService.shutdown();
        log.info("all services registered successfully");
        return;
      }

      for (final AnnotatedType<?> type : managedTypes.getServiceEndpoints()) {
        if (!registered.contains(type) || beanManager.getBeans(type.getJavaClass()).size() == 0) {
          continue;
        }

        final MessageCallback callback = (MessageCallback) CDIServerUtil.lookupBean(beanManager,
                type.getJavaClass());

        registered.remove(type);

        // Discriminate on @Command
        final Map<String, Method> commandPoints = new HashMap<String, Method>();
        for (final AnnotatedMethod method : type.getMethods()) {
          if (method.isAnnotationPresent(Command.class)) {
            final Command command = method.getAnnotation(Command.class);
            for (String cmdName : command.value()) {
              if (cmdName.equals(""))
                cmdName = method.getJavaMember().getName();
              commandPoints.put(cmdName, method.getJavaMember());
            }
          }
        }

        final String subjectName = CDIServerUtil.resolveServiceName(type.getJavaClass());

        if (commandPoints.isEmpty()) {
          bus.subscribe(subjectName, callback);
        }
        else {
          bus.subscribeLocal(subjectName, new CommandBindingsCallback(commandPoints, callback, bus));
        }
      }
    }
  }

  private void subscribeServices(final BeanManager beanManager, final MessageBus bus) {
    for (final Map.Entry<AnnotatedType, List<AnnotatedMethod>> entry : managedTypes.getServiceMethods().entrySet()) {
      final Class<?> type = entry.getKey().getJavaClass();

      for (final AnnotatedMethod method : entry.getValue()) {
        final Service svc = method.getAnnotation(Service.class);
        final String svcName = svc.value().equals("") ? method.getJavaMember().getName() : svc.value();

        final Method callMethod = method.getJavaMember();

        bus.subscribe(svcName, new MessageCallback() {

          @Override
          public void callback(final Message message) {
            final Object targetBean = CDIServerUtil.lookupBean(beanManager, type);

            try {
              callMethod.invoke(targetBean, message);
            }
            catch (Exception e) {
              ErrorHelper.sendClientError(bus, message, "Error dispatching service", e);
            }
          }
        });
      }
    }

    /**
     * Due to the lack of contract in CDI guaranteeing when beans will be available, we use an executor to search for
     * the beans every 100ms until it finds them. Or, after a 25 seconds, blow up if they don't become available.
     */
    final ScheduledExecutorService startupScheduler = Executors.newScheduledThreadPool(1);
    startupScheduler.scheduleAtFixedRate(
            new StartupCallback(beanManager, bus, startupScheduler, 25), 0, 100, TimeUnit.MILLISECONDS);

    for (final Class<?> remoteInterfaceType : managedTypes.getRemoteInterfaces()) {
      createRPCScaffolding(remoteInterfaceType, bus, beanManager);
    }
  }


  private void createRPCScaffolding(final Class remoteIface, final MessageBus bus, final BeanManager beanManager) {
    final Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();

    // beware of classloading issues. better reflect on the actual instance
    for (final Method method : remoteIface.getMethods()) {
      if (RebindUtils.isMethodInInterface(remoteIface, method)) {

        epts.put(RebindUtils.createCallSignature(remoteIface, method), new ConversationalEndpointCallback(
                new ServiceInstanceProvider() {
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

                }, method, bus));
      }
    }

    final RemoteServiceCallback delegate = new RemoteServiceCallback(epts);
    bus.subscribe(remoteIface.getName() + ":RPC", new MessageCallback() {
      @Override
      public void callback(final Message message) {
        delegate.callback(message);
      }
    });

    // note: this method just exists because we want AbstractRemoteCallBuilder to be package private.
    DefaultRemoteCallBuilder.setProxyFactory(Assert.notNull(new ProxyFactory() {
      @Override
      public <T> T getRemoteProxy(final Class<T> proxyType) {
        throw new RuntimeException(
                "There is not yet an available Errai RPC implementation for the server-side environment.");
      }
    }));
  }

  static class EventConsumer {
    private final boolean conversational;
    private final Type eventBeanType;
    private final Type eventType;
    private final Annotation[] qualifiers;

    EventConsumer(final boolean conversational,
                  final Type eventBeanType,
                  final Type type,
                  final Annotation[] qualifiers) {

      this.conversational = conversational;
      this.eventBeanType = eventBeanType;
      this.eventType = type;
      this.qualifiers = qualifiers;
    }

    public boolean isConversational() {
      return conversational;
    }

    public Class<?> getRawType() {
      if (eventType instanceof Class) {
        return (Class) eventType;
      }
      else if (eventType instanceof ParameterizedType) {
        return (Class) ((ParameterizedType) eventType).getRawType();
      }
      else {
        throw new RuntimeException("bad type: " + eventType);
      }
    }

    public Type getEventBeanType() {
      return eventBeanType;
    }

    public Annotation[] getQualifiers() {
      return qualifiers;
    }

    @Override
    public String toString() {
      return "EventConsumer " + eventType + " " + Arrays.toString(qualifiers);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o)
        return true;
      if (!(o instanceof EventConsumer))
        return false;

      final EventConsumer that = (EventConsumer) o;

      return that.toString().equals(toString());
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }
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
