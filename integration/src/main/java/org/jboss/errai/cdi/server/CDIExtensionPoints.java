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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.builder.AbstractRemoteCallBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.ProxyProvider;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.util.ErrorHelper;
import org.jboss.errai.bus.rebind.RebindUtils;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.ExposeEntity;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.CommandBindingsCallback;
import org.jboss.errai.bus.server.io.ConversationalEndpointCallback;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.client.api.Conversational;
import org.jboss.errai.cdi.server.events.ShutdownEventObserver;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Conversation;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Extension points to the CDI container.
 * Makes Errai components available as CDI beans (i.e. the message bus)
 * and registers CDI components as services with Errai.
 *
 * @author Heiko.Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
@ApplicationScoped
public class CDIExtensionPoints implements Extension {
    private static final Logger log = LoggerFactory.getLogger(CDIExtensionPoints.class);

    private TypeRegistry managedTypes = null;
    private String uuid = null;
    private ContextManager contextManager;
    private ErraiService service;

    private Map<Class<?>, Class<?>> conversationalObservers = new HashMap<Class<?>, Class<?>>();
    private Set<Class<?>> conversationalServices = new HashSet<Class<?>>();
    private Set<String> observableEvents = new HashSet<String>();

    private static final Set<String> vetoClasses;

    static {
        Set<String> veto = new HashSet<String>();
        veto.add(ServerMessageBusImpl.class.getName());
        veto.add(RequestDispatcher.class.getName());
        veto.add(ErraiService.class.getName());

        vetoClasses = Collections.unmodifiableSet(veto);
    }


    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        this.uuid = UUID.randomUUID().toString();
        this.managedTypes = new TypeRegistry();

        log.info("Created Errai-CDI context: " + uuid);
    }

    /**
     * Register managed beans as Errai services
     *
     * @param event
     * @param <T>
     */
    public <T> void observeResources(@Observes ProcessAnnotatedType<T> event) {
        final AnnotatedType<T> type = event.getAnnotatedType();

        // services
        if (type.isAnnotationPresent(Service.class)) {
            log.debug("Discovered Errai annotation on type: " + type);
            boolean isRpc = false;

            Class<T> javaClass = type.getJavaClass();
            for (Class<?> intf : javaClass.getInterfaces()) {
                isRpc = intf.isAnnotationPresent(Remote.class);

                if (isRpc) {
                    log.debug("Identified Errai RPC interface: " + intf + " on " + type);
                    managedTypes.addRPCEndpoint(intf, type);
                }
            }

            if (!isRpc) {
                managedTypes.addServiceEndpoint(type);
            }

            // enforce application scope until we get the other scopes working
//            ApplicationScoped scope = type.getAnnotation(ApplicationScoped.class);
//            if (null == scope)
//                log.warn("Service implementation not @ApplicationScoped: " + type.getJavaClass());

        } else {

            for (AnnotatedMethod method : type.getMethods()) {
                if (method.isAnnotationPresent(Service.class)) {
                    managedTypes.addServiceMethod(type, method);
                }

            }
        }


        // veto on client side implementations that contain CDI annotations
        // (i.e. @Observes) Otherwise Weld might try to invoke on them


        if (vetoClasses.contains(type.getJavaClass().getName())
                || (type.getJavaClass().getPackage().getName().contains("client")
                && !type.getJavaClass().isInterface())) {
            event.veto();
            log.info("Veto " + type);
        }

        /**
         * We must scan for Event injection points to build the tables
         */
        Class clazz = type.getJavaClass();

        for (Field f : clazz.getDeclaredFields()) {
            if (Event.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(Inject.class)) {
                ParameterizedType pType = (ParameterizedType) f.getGenericType();

                Class eventType = (Class) pType.getActualTypeArguments()[0];

                if (isExposedEntityType(eventType)) {
                    observableEvents.add(eventType.getName());
                }
            }
        }
    }

    ;


    private boolean isExposedEntityType(Class type) {
        if (type.isAnnotationPresent(ExposeEntity.class)) {
            return true;
        } else {
            if (String.class.equals(type) || TypeHandlerFactory.getHandler(type) != null) {
                return true;
            }
        }
        return false;
    }

    public void processObserverMethod(@Observes ProcessObserverMethod processObserverMethod) {
        Type t = processObserverMethod.getObserverMethod().getObservedType();

        if (t instanceof Class && ConversationalEvent.class.isAssignableFrom((Class) t)) {
            throw new RuntimeException("observing unqualified ConversationalEvent. You must specify type parameters");
        }

        Class type = null;

        if (t instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) t;
            if (ConversationalEvent.class.isAssignableFrom((Class) pType.getRawType())) {
                Type[] tArgs = pType.getActualTypeArguments();
                conversationalObservers.put(type = (Class) tArgs[0], (Class) tArgs[1]);
            }
        }

        if (type == null && t instanceof Class) {
            type = (Class) t;
        }

        if (isExposedEntityType(type)) {
            observableEvents.add(type.getName());
        }

        if (processObserverMethod.getAnnotatedMethod().isAnnotationPresent(Conversational.class)) {
            conversationalServices.add(type);
        }
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        // Errai Service wrapper

        this.service = Util.lookupErraiService();

        final MessageBus bus = service.getBus();

        if (bus.isSubscribed(CDI.DISPATCHER_SUBJECT)) {
            return;
        }

        QueueSessionContext sessionContext = new QueueSessionContext();

        abd.addContext(sessionContext);

        abd.addBean(new ServiceMetaData(bm, this.service));

        // context handling hooks
        this.contextManager = new ContextManager(uuid, bm, bus, sessionContext);

        // Custom Reply
        abd.addBean(new ConversationMetaData(bm, new ErraiConversation(
                (Conversation) Util.lookupCallbackBean(bm, Conversation.class),
                this.contextManager
        )));

        // event dispatcher
        EventDispatcher eventDispatcher = new EventDispatcher(bm, bus, this.contextManager, observableEvents);

        for (Map.Entry<Class<?>, Class<?>> entry : conversationalObservers.entrySet()) {
            eventDispatcher.registerConversationEvent(entry.getKey(), entry.getValue());
        }

        for (Class<?> entry : conversationalServices) {
            eventDispatcher.registerConversationalService(entry);
        }

        EventSubscriptionListener listener = new EventSubscriptionListener(abd, bus, contextManager, observableEvents);
        bus.addSubscribeListener(listener);


        // Errai bus injection
        abd.addBean(new MessageBusMetaData(bm, bus));

        // Support to inject the request dispatcher.
        abd.addBean(new RequestDispatcherMetaData(bm, service.getDispatcher()));

        // Register observers        
        abd.addObserverMethod(new ShutdownEventObserver(managedTypes, bus, uuid));

        // subscribe service and rpc endpoints
        subscribeServices(bm, bus);

        // subscribe event dispatcher
        bus.subscribe(CDI.DISPATCHER_SUBJECT, eventDispatcher);
    }

    private void subscribeServices(final BeanManager beanManager, final MessageBus bus) {
        for (Map.Entry<AnnotatedType, List<AnnotatedMethod>> entry : managedTypes.getServiceMethods().entrySet()) {
            final Class<?> type = entry.getKey().getJavaClass();

            for (final AnnotatedMethod method : entry.getValue()) {
                Service svc = method.getAnnotation(Service.class);
                String svcName = svc.value().equals("") ? method.getJavaMember().getName() : svc.value();

                final Method callMethod = method.getJavaMember();

                if (isApplicationScoped(entry.getKey())) {
                    /**
                     * Register the endpoint as a ApplicationScoped bean.
                     */

                    bus.subscribe(svcName, new MessageCallback() {
                        volatile Object targetBean;

                        public void callback(Message message) {
                            if (targetBean == null) {
                                targetBean = Util.lookupCallbackBean(beanManager, type);
                            }

                            try {
                                contextManager.activateRequestContext();
                                callMethod.invoke(targetBean, message);
                            } catch (Exception e) {
                                ErrorHelper.sendClientError(bus, message, "Error dispatching service", e);
                            } finally {
                                contextManager.deactivateRequestContext();
                            }
                        }
                    });
                } else {
                    /**
                     * Register the endpoint as a passivating scoped bean.
                     */
                    bus.subscribe(svcName, new MessageCallback() {
                        public void callback(Message message) {
                            try {
                                contextManager.activateRequestContext();
                                contextManager.activateSessionContext(message);

                                callMethod.invoke(Util.lookupCallbackBean(beanManager, type), message);
                            } catch (Exception e) {
                                ErrorHelper.sendClientError(bus, message, "Error dispatching service", e);
                            } finally {
                                contextManager.deactivateRequestContext();
                            }
                        }
                    });
                }

            }

        }


        for (final AnnotatedType<?> type : managedTypes.getServiceEndpoints()) {
            // Discriminate on @Command
            Map<String, Method> commandPoints = new HashMap<String, Method>();
            for (final AnnotatedMethod method : type.getMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    Command command = method.getAnnotation(Command.class);
                    for (String cmdName : command.value()) {
                        if (cmdName.equals("")) cmdName = method.getJavaMember().getName();
                        commandPoints.put(cmdName, method.getJavaMember());
                    }
                }
            }


            log.info("Register MessageCallback: " + type);
            String subjectName = Util.resolveServiceName(type.getJavaClass());

            if (isApplicationScoped(type)) {
             //   Object targetbean = Util.lookupCallbackBean(beanManager, type.getJavaClass());

//                final MessageCallback invocationTarget = commandPoints.isEmpty() ?
//                        (MessageCallback) targetbean : new CommandBindingsCallback(commandPoints, targetbean);


                // TODO: enable CommandBindings
                bus.subscribe(subjectName, new MessageCallback() {
                    volatile MessageCallback callback;

                    public void callback(final Message message) {
                        if (callback == null) {
                            callback = (MessageCallback) Util.lookupCallbackBean(beanManager, type.getJavaClass());
                        }

                        contextManager.activateSessionContext(message);
                        contextManager.activateRequestContext();
                        contextManager.activateConversationContext(message);
                        try {
                            callback.callback(message);
                        } finally {
                            contextManager.deactivateRequestContext();
                            contextManager.deactivateConversationContext(message);
                        }
                    }
                });
            } else {
                /**
                 * Map passitivating scope.
                 */
                bus.subscribe(subjectName, new MessageCallback() {
                    public void callback(final Message message) {
                        contextManager.activateSessionContext(message);
                        contextManager.activateRequestContext();
//                        contextManager.activateConversationContext(message);
                        try {
                            ((MessageCallback) Util.lookupCallbackBean(beanManager, type.getJavaClass())).callback(message);
                        } finally {
                            contextManager.deactivateRequestContext();
                            contextManager.deactivateConversationContext(message);
                        }
                    }
                });

            }

        }

        //todo: needs to be rewritten to support @SessionScoped
        for (final Class<?> rpcIntf : managedTypes.getRpcEndpoints().keySet()) {
            final AnnotatedType type = managedTypes.getRpcEndpoints().get(rpcIntf);
            final Class beanClass = type.getJavaClass();

            log.info("Register RPC Endpoint: " + type + "(" + rpcIntf + ")");

            // TODO: Copied from errai internals, refactor at some point
            createRPCScaffolding(rpcIntf, beanClass, bus, new ResourceProvider() {
                public Object get() {
                    return Util.lookupRPCBean(beanManager, rpcIntf, beanClass);
                }
            });
        }
    }

    private void createRPCScaffolding(final Class remoteIface, final Class<?> type, final MessageBus bus,
                                      final ResourceProvider resourceProvider) {

        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageBus.class).toInstance(bus);
                //bind(RequestDispatcher.class).toInstance(context.getService().getDispatcher());

                bind(type).toProvider(new Provider() {
                    public Object get() {
                        return resourceProvider.get();
                    }
                });
            }
        });

        Object svc = injector.getInstance(type);

        Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();

        // beware of classloading issues. better reflect on the actual instance
        for (Class<?> intf : svc.getClass().getInterfaces()) {
            for (final Method method : intf.getDeclaredMethods()) {
                if (RebindUtils.isMethodInInterface(remoteIface, method)) {
                    epts.put(RebindUtils.createCallSignature(method),
                            new ConversationalEndpointCallback(svc, method, bus));
                }
            }
        }

        final RemoteServiceCallback delegate = new RemoteServiceCallback(epts);
        bus.subscribe(remoteIface.getName() + ":RPC", new MessageCallback() {
            public void callback(Message message) {
                try {
                    CDIExtensionPoints.this.contextManager.activateRequestContext();
                    delegate.callback(message);
                } finally {
                    CDIExtensionPoints.this.contextManager.deactivateRequestContext();
                }
            }
        });

        new ProxyProvider() {
            {
                AbstractRemoteCallBuilder.setProxyFactory(this);
            }

            public <T> T getRemoteProxy(Class<T> proxyType) {
                throw new RuntimeException("This API is not supported in the server-side environment.");
            }
        };
    }


    private static boolean isApplicationScoped(AnnotatedType type) {
        return type.isAnnotationPresent(ApplicationScoped.class);
    }

    class BeanLookup {
        private BeanManager beanManager;
        private AnnotatedType<?> type;

        private Object invocationTarget;

        BeanLookup(AnnotatedType<?> type, BeanManager bm) {
            this.type = type;
            this.beanManager = bm;
        }

        public Object getInvocationTarget() {
            if (null == invocationTarget) {
                invocationTarget =
                        Util.lookupCallbackBean(beanManager, type.getJavaClass());
            }
            return invocationTarget;
        }
    }
}
