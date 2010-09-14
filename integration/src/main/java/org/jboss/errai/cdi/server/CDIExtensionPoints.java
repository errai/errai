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
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.CommandBindingsCallback;
import org.jboss.errai.bus.server.io.ConversationalEndpointCallback;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

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

    private List<AnnotatedType> services = new ArrayList<AnnotatedType>();
    private Map<Class<?>, AnnotatedType> rpcEndpoints = new HashMap<Class<?>, AnnotatedType>();

    private EventDispatcher eventDispatcher;

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd)
    {
        //System.out.println("** beginning the scanning process");
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
                    rpcEndpoints.put(intf, type);
                }
            }

            if(!isRpc)
            {
                services.add(type);
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
        // Errai bus injection
        provideErraiBus(abd, bm);

        // Eventing support
        observeEvents(abd, bm);

        registerServices(bm);

        registerEventDispatcher(bm, lookupMessageBus());
    }

    private void registerEventDispatcher(BeanManager bm, MessageBus messageBus)
    {
        this.eventDispatcher = new EventDispatcher(bm, messageBus);
        messageBus.subscribe("cdi.event:Dispatcher", eventDispatcher);
    }

    private MessageBus lookupMessageBus()
    {
        InitialContext ctx = null;
        ErraiService errai = null;

        try
        {
            ctx = new InitialContext();
            errai = (ErraiService)ctx.lookup("java:/Errai");
        }
        catch (NamingException e)
        {
            if(ctx!=null)
            {
                try
                {
                    errai = (ErraiService)ctx.lookup("java:comp/env/Errai"); // development mode
                }
                catch (NamingException e1)
                {
                }
            }

            if(null==errai)
                throw new RuntimeException("Failed to locate Errai service instance", e);
        }

        return errai.getBus();
    }

    private void registerServices(final BeanManager beanManager)
    {
        MessageBus bus = lookupMessageBus();

        for(final AnnotatedType<?> type : services)
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
            String subjectName = resolveServiceName(type.getJavaClass());

            Object targetbean = lookupCallbackBean(beanManager, type.getJavaClass());
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

        for(final Class<?> rpcIntf : rpcEndpoints.keySet())
        {
            final AnnotatedType type = rpcEndpoints.get(rpcIntf);
            final Class beanClass = type.getJavaClass();

            log.info("Register RPC Endpoint: " + type + "("+rpcIntf+")");

            // TODO: Copied from errai internals, refactor at some point
            createRPCScaffolding(rpcIntf, beanClass, bus, new ResourceProvider()
            {
                public Object get()
                {
                    return lookupRPCBean(beanManager, rpcIntf, beanClass);
                }
            });
        }
    }

    public static void activateContexts(boolean active)
    {
        // TODO: Does this work in AS 6 ?
        final ContextLifecycle contextLifecycle = Container.instance().services().get(ContextLifecycle.class);

        // request
        RequestContext requestContext = contextLifecycle.getRequestContext();
        requestContext.setActive(active);

        // conversation
        ConversationContext conversationContext = contextLifecycle.getConversationContext();
        conversationContext.setActive(active);

        // TODO: session?
    }

    private String resolveServiceName(Class<?> type)
    {
        String subjectName = type.getAnnotation(Service.class).value();
        if(subjectName.equals("")) subjectName = type.getSimpleName();
        return subjectName;
    }

    private Object lookupCallbackBean(BeanManager beanManager, Class<?> serviceType)
    {
        Set<Bean<?>> beans = beanManager.getBeans(serviceType);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> context = beanManager.createCreationalContext(bean);

        return beanManager.getReference(bean, serviceType, context);
    }

    private <T> T lookupRPCBean(BeanManager beanManager, T rpcIntf, Class beanClass)
    {
        Set<Bean<?>> beans = beanManager.getBeans(beanClass);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> context = beanManager.createCreationalContext(bean);
        return (T)beanManager.getReference(bean, beanClass, context);

    }

    private void observeEvents(AfterBeanDiscovery abd, BeanManager bm)
    {
        abd.addObserverMethod(
                new ObserverMethod()
                {
                    public Class<?> getBeanClass()
                    {
                        return CDIExtensionPoints.class;
                    }

                    public Type getObservedType()
                    {
                        return Outbound.class;
                    }

                    public Set<Annotation> getObservedQualifiers()
                    {
                        Set<Annotation> qualifiers = new HashSet<Annotation>();
                        return qualifiers;
                    }

                    public Reception getReception()
                    {
                        return Reception.ALWAYS;
                    }

                    public TransactionPhase getTransactionPhase()
                    {
                        return null;
                    }

                    public void notify(Object o)
                    {
                        if(null==eventDispatcher)
                            throw new RuntimeException("EventDispatcher not initialized");
                        eventDispatcher.sendMessage((Outbound)o);
                    }
                }
        );

        // Shutdownhook
        abd.addObserverMethod(
                new ObserverMethod()
                {
                    public Class<?> getBeanClass()
                    {
                        return CDIExtensionPoints.class;
                    }

                    public Type getObservedType()
                    {
                        return BeforeShutdown.class;
                    }

                    public Set<Annotation> getObservedQualifiers()
                    {
                        Set<Annotation> qualifiers = new HashSet<Annotation>();
                        return qualifiers;
                    }

                    public Reception getReception()
                    {
                        return Reception.ALWAYS;
                    }

                    public TransactionPhase getTransactionPhase()
                    {
                        return null;
                    }

                    public void notify(Object o)
                    {
                        MessageBus bus = lookupMessageBus();
                        for(AnnotatedType<?> svc : services)
                        {
                            String subject = resolveServiceName(svc.getJavaClass());
                            log.debug("Unsubscribe: "+subject);
                            bus.unsubscribeAll(subject);
                        }

                        for(Class<?> rpcIntf : rpcEndpoints.keySet())
                        {
                            String rpcSubjectName = rpcIntf.getName() + ":RPC";
                            log.debug("Unsubscribe: "+rpcSubjectName);
                            bus.unsubscribeAll(rpcSubjectName);
                        }
                    }
                }
        );
    }

    private void provideErraiBus(AfterBeanDiscovery abd, BeanManager bm)
    {
        //use this to read annotations of the class
        AnnotatedType at = bm.createAnnotatedType(ServerMessageBusImpl.class);

        //use this to create the class and inject dependencies
        final InjectionTarget it = bm.createInjectionTarget(at);

        abd.addBean( new Bean() {


            public Class<?> getBeanClass() {
                return MessageBus.class;
            }


            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            public String getName() {
                return null;
            }

            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add( new AnnotationLiteral<Default>() {} );
                qualifiers.add( new AnnotationLiteral<Any>() {} );
                return qualifiers;
            }

            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(MessageBus.class);
                types.add(Object.class);
                return types;
            }

            public boolean isAlternative() {
                return false;
            }

            public boolean isNullable() {
                return false;
            }

            public Object create(CreationalContext ctx) {
                Object instance = lookupMessageBus();
                it.inject(instance, ctx);
                it.postConstruct(instance);
                return instance;
            }

            public void destroy(Object instance, CreationalContext ctx) {
                it.preDestroy(instance);
                it.dispose(instance);
                ctx.release();
            }
        }

        );
    }


    public <T> void observeInjectionTarget(@Observes ProcessInjectionTarget<T> event)
    {
        //System.out.println("\t -> "+ event.getAnnotatedType().getJavaClass());
    }

    public static ServiceRegistry lookupServiceRegistry(BeanManager manager)
    {
        Set<Bean<?>> beans = manager.getBeans(ServiceRegistry.class);
        Bean<?> bean = manager.resolve(beans);
        if(null==bean)
            throw new IllegalArgumentException("Failed to lookup ServiceRegistry");

        CreationalContext<?> context = manager.createCreationalContext(bean);
        return (ServiceRegistry) manager.getReference(bean, ServiceRegistry.class, context);
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
