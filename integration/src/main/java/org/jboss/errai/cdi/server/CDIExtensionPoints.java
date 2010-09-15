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
import org.jboss.errai.bus.rebind.RebindUtils;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.CommandBindingsCallback;
import org.jboss.errai.bus.server.io.ConversationalEndpointCallback;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.cdi.server.events.OutboundEventObserver;
import org.jboss.errai.cdi.server.events.ShutdownEventObserver;
import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Extension points to the CDI container.
 * Makes Errai components available as CDI beans (i.e. the message bus)
 * and registers CDI components as services with Errai.
 *
 * @author Heiko.Braun <hbraun@redhat.com>
 */
@ApplicationScoped
public class CDIExtensionPoints implements Extension
{
    private static final Logger log = LoggerFactory.getLogger(CDIExtensionPoints.class);

    private TypeRegistry managedTypes = null;    

    private String uuid = null;

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd)
    {
        this.uuid = UUID.randomUUID().toString();
        this.managedTypes = new TypeRegistry();
        
        log.info("Created Errai-CDI context: " +uuid);
    }

    /**
     * Register managed beans as Errai services
     * @param event
     * @param <T>
     */
    public <T> void observeResources(@Observes ProcessAnnotatedType<T> event)
    {
        final AnnotatedType<T> type = event.getAnnotatedType();

        // services
        if(type.isAnnotationPresent(Service.class))
        {
            log.debug("Discovered Errai Annotation on type: "+ type);
            boolean isRpc = false;

            Class<T> javaClass = type.getJavaClass();
            for(Class<?> intf : javaClass.getInterfaces())
            {
                isRpc = intf.isAnnotationPresent(Remote.class);

                if(isRpc)
                {
                    log.debug("Identified Errai RPC interface: " + intf + " on "+type);
                    managedTypes.addRPCEndpoint(intf, type);
                }
            }

            if(!isRpc)
            {
                managedTypes.addServiceEndpoint(type);
            }

            // enforce application scope until we get the other scopes working
            ApplicationScoped scope = type.getAnnotation(ApplicationScoped.class);
            if(null==scope)
                log.warn("Service implementation not @ApplicationScoped: "+type.getJavaClass());

        }

        /**
         * Mixing JSR-299 and Errai annotation causes bean valdation problems.
         * Therefore we need to provide additional meta data for the Provider implementations,
         * (the Produces annotation literal)
         * even though these classes are only client side implementations.
         */
        /*else if(type.isAnnotationPresent(org.jboss.errai.ioc.client.api.Provider.class))
       {
         AnnotatedTypeBuilder<T> builder = AnnotatedTypeBuilder.newInstance(event.getAnnotatedType().getJavaClass());
         builder.readAnnotationsFromUnderlyingType();

         //builder.addToClass(new ApplicationScopedQualifier(){});
         for(AnnotatedMethod method : type.getMethods())
         {
           if("provide".equals(method.getJavaMember().getName()))
           {
             builder.addToMethod(method.getJavaMember(), new ProducesQualifier(){});
             break;
           }
         }
         AnnotatedType<T> replacement = builder.create();
         event.setAnnotatedType(replacement);
       } */
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd,  BeanManager bm)
    {
        final MessageBus bus = Util.lookupMessageBus();
        EventDispatcher eventDispatcher = new EventDispatcher(bm, bus);

        // Errai bus injection
        abd.addBean(new MessageBusDelegate(bm, bus));

        // Register observers
        abd.addObserverMethod(new OutboundEventObserver(eventDispatcher));
        abd.addObserverMethod(new ShutdownEventObserver(managedTypes, bus, uuid));

        // subscribe service and rpc endpoints
        subscribeServices(bm, bus);

        // subscribe event dispatcher
        bus.subscribe(EventDispatcher.NAME, eventDispatcher);
    }

    private void subscribeServices(final BeanManager beanManager, final MessageBus bus)
    {        
        for(final AnnotatedType<?> type : managedTypes.getServiceEndpoints())
        {
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

            Object targetbean = Util.lookupCallbackBean(beanManager, type.getJavaClass());
            final MessageCallback invocationTarget = commandPoints.isEmpty() ?
                    (MessageCallback)targetbean : new CommandBindingsCallback(commandPoints, targetbean);

            bus.subscribe(subjectName, new MessageCallback()
            {
                public void callback(final Message message) {
                    //ServletContext context = message.getResource(ServletContext.class, "errai.experimental.servletContext");
                    activateContexts(true);
                    try {
                        invocationTarget.callback(message);
                    } finally {
                        activateContexts(false);
                    }
                }
            });

        }

        for(final Class<?> rpcIntf : managedTypes.getRpcEndpoints().keySet())
        {
            final AnnotatedType type = managedTypes.getRpcEndpoints().get(rpcIntf);
            final Class beanClass = type.getJavaClass();

            log.info("Register RPC Endpoint: " + type + "("+rpcIntf+")");

            // TODO: Copied from errai internals, refactor at some point
            createRPCScaffolding(rpcIntf, beanClass, bus, new ResourceProvider()
            {
                public Object get()
                {
                    return Util.lookupRPCBean(beanManager, rpcIntf, beanClass);
                }
            });
        }
    }

    public static void activateContexts(boolean active)
    {        
        final ContextLifecycle contextLifecycle = Container.instance().services().get(ContextLifecycle.class);

        // request
        RequestContext requestContext = contextLifecycle.getRequestContext();
        requestContext.setActive(active);

        // conversation
        ConversationContext conversationContext = contextLifecycle.getConversationContext();
        conversationContext.setActive(active);

        // TODO: session?
    }
    
    private void createRPCScaffolding(final Class remoteIface, final Class<?> type, final MessageBus bus, final ResourceProvider resourceProvider) {

        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageBus.class).toInstance(bus);
                //bind(RequestDispatcher.class).toInstance(context.getService().getDispatcher());

                bind(type).toProvider(new Provider()
                {
                    public Object get()
                    {
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
        bus.subscribe(remoteIface.getName() + ":RPC", new MessageCallback()
        {
            public void callback(Message message) {
                try {
                    activateContexts(true);
                    delegate.callback(message);
                } finally {
                    activateContexts(false);
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
}
