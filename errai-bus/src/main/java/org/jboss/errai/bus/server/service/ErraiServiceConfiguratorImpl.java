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

import com.google.inject.*;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.builder.AbstractRemoteCallBuilder;
import org.jboss.errai.bus.client.framework.*;
import org.jboss.errai.bus.rebind.RebindUtils;
import org.jboss.errai.bus.server.*;
import org.jboss.errai.bus.server.Module;
import org.jboss.errai.bus.server.annotations.*;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;
import org.jboss.errai.bus.server.io.ConversationalEndpointCallback;
import org.jboss.errai.bus.server.io.EndpointCallback;
import org.jboss.errai.bus.server.io.JSONEncoder;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.security.auth.rules.RolesRequiredRule;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.ConfigVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.inject.Guice.createInjector;
import static java.util.ResourceBundle.getBundle;
import static org.jboss.errai.bus.server.util.ConfigUtil.visitAllTargets;

/**
 * Default implementation of the ErraiBus server-side configurator.
 */
public class ErraiServiceConfiguratorImpl implements ErraiServiceConfigurator {
    private ServerMessageBus bus;
    private List<File> configRootTargets;
    private Map<String, String> properties;

    private Map<Class, Provider> extensionBindings;
    private Map<String, Provider> resourceProviders;
    private Set<Class> serializableTypes;

    private RequestDispatcher dispatcher;
    private SessionProvider sessionProvider;

    private ErraiServiceConfigurator configInst = this;

    private boolean autoScanModules = true;

    private Logger log = LoggerFactory.getLogger(ErraiServiceConfigurator.class);

    /**
     * Initializes the <tt>ErraiServiceConfigurator</tt> with a specified <tt>ServerMessageBus</tt>
     *
     * @param bus - the server message bus in charge of transmitting messages
     */
    @Inject
    public ErraiServiceConfiguratorImpl(ServerMessageBus bus) {
        this.bus = bus;
    }

    /**
     * Configures the specified service and the bus. All components and extensions are loaded, also anything that
     * needs to be done during the initialization stage. The configuration is read in and everything is set up
     *
     * @param erraiService - the service associated with the bus
     */
    public void configure(final ErraiService erraiService) {
        properties = new HashMap<String, String>();
        configRootTargets = ConfigUtil.findAllConfigTargets();

        try {
            String bundlePath = System.getProperty("errai.service_config_prefix_path");
            ResourceBundle erraiServiceConfig = getBundle(bundlePath == null ? "ErraiService" : bundlePath + ".ErraiService");
            Enumeration<String> keys = erraiServiceConfig.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                properties.put(key, erraiServiceConfig.getString(key));
            }
        }
        catch (Exception e) {
            throw new ErraiBootstrapFailure("Error reading from configuration. Did you include ErraiService.properties?", e);
        }


        // Create a Map to collect any extensions bindings to be bound to
        // services.
        extensionBindings = new HashMap<Class, Provider>();
        resourceProviders = new HashMap<String, Provider>();
        serializableTypes = new HashSet<Class>();
        final Set<String> loadedComponents = new HashSet<String>();
        final List<Runnable> deferred = new LinkedList<Runnable>();

        if (properties.containsKey("errai.authentication_adapter")) {
            try {
                final Class<? extends AuthenticationAdapter> authAdapterClass = Class.forName(properties.get("errai.authentication_adapter"))
                        .asSubclass(AuthenticationAdapter.class);

                log.info("authentication adapter configured: " + authAdapterClass.getName());

                final Runnable create = new Runnable() {
                    public void run() {
                        final AuthenticationAdapter authAdapterInst = Guice.createInjector(new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(AuthenticationAdapter.class).to(authAdapterClass);
                                bind(ErraiServiceConfigurator.class).toInstance(configInst);
                                bind(MessageBus.class).toInstance(bus);
                            }
                        }).getInstance(AuthenticationAdapter.class);

                        extensionBindings.put(AuthenticationAdapter.class, new Provider() {
                            public Object get() {
                                return authAdapterInst;
                            }
                        });
                    }
                };

                try {
                    create.run();
                }
                catch (CreationException e) {
                    log.info("authentication adapter " + authAdapterClass.getName() + " cannot be bound yet, deferring ...");
                    deferred.add(create);
                }
            }
            catch (ErraiBootstrapFailure e) {
                throw e;
            }
            catch (Exception e) {
                throw new ErraiBootstrapFailure("cannot configure authentication adapter", e);
            }
        }

        this.dispatcher = createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Class<? extends RequestDispatcher> dispatcherImplementation = SimpleDispatcher.class;

                if (configInst.hasProperty(ERRAI_DISPATCHER_IMPLEMENTATION)) {
                    try {
                        dispatcherImplementation = Class.forName(configInst.getProperty(ERRAI_DISPATCHER_IMPLEMENTATION))
                                .asSubclass(RequestDispatcher.class);
                    }
                    catch (Exception e) {
                        throw new ErraiBootstrapFailure("could not load request dispatcher implementation class", e);
                    }
                }

                log.info("using dispatcher implementation: " + dispatcherImplementation.getName());

                bind(RequestDispatcher.class).to(dispatcherImplementation);
                bind(ErraiService.class).toInstance(erraiService);
                bind(MessageBus.class).toInstance(bus);
                bind(ErraiServiceConfigurator.class).toInstance(configInst);
            }
        }).getInstance(RequestDispatcher.class);

        this.sessionProvider = createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Class<? extends SessionProvider> sessionProviderImplementation = HttpSessionProvider.class;

                if (configInst.hasProperty(ERRAI_SESSION_PROVIDER_IMPLEMENTATION)) {
                    try {
                        sessionProviderImplementation = Class.forName(configInst.getProperty(ERRAI_SESSION_PROVIDER_IMPLEMENTATION))
                                .asSubclass(SessionProvider.class);
                    }
                    catch (Exception e) {
                        throw new ErraiBootstrapFailure("could not load session provider implementation class", e);
                    }
                }

                log.info("using session provider implementation: " + sessionProviderImplementation.getName());

                bind(SessionProvider.class).to(sessionProviderImplementation);
                bind(ErraiService.class).toInstance(erraiService);
                bind(MessageBus.class).toInstance(bus);
                bind(ErraiServiceConfigurator.class).toInstance(configInst);
            }
        }).getInstance(SessionProvider.class);

        log.info("beging searching for Errai extensions ...");

        if (properties.containsKey("errai.auto_scan_modules")) {
            autoScanModules = Boolean.parseBoolean(properties.get("errai.auto_scan_modules"));
        }
        if (autoScanModules) {

            // Search for Errai extensions.
            visitAllTargets(configRootTargets, new ConfigVisitor() {
                public void visit(Class<?> loadClass) {
                    if (ErraiConfigExtension.class.isAssignableFrom(loadClass)
                            && loadClass.isAnnotationPresent(ExtensionComponent.class) && !loadedComponents.contains(loadClass.getName())) {

                        loadedComponents.add(loadClass.getName());

                        // We have an annotated ErraiConfigExtension.  So let's configure it.
                        final Class<? extends ErraiConfigExtension> clazz =
                                loadClass.asSubclass(ErraiConfigExtension.class);


                        log.info("found extension " + clazz.getName());

                        try {

                            final Runnable create = new Runnable() {
                                public void run() {
                                    AbstractModule module = new AbstractModule() {
                                        @Override
                                        protected void configure() {
                                            bind(ErraiConfigExtension.class).to(clazz);
                                            bind(ErraiServiceConfigurator.class).toInstance(configInst);
                                            bind(MessageBus.class).toInstance(bus);

                                            // Add any extension bindings.
                                            for (Map.Entry<Class, Provider> entry : extensionBindings.entrySet()) {
                                                bind(entry.getKey()).toProvider(entry.getValue());
                                            }
                                        }
                                    };
                                    Guice.createInjector(module)
                                            .getInstance(ErraiConfigExtension.class)
                                            .configure(extensionBindings, resourceProviders);
                                }
                            };

                            try {
                                create.run();
                            }
                            catch (CreationException e) {
                                log.info("extension " + clazz.getName() + " cannot be bound yet, deferring ...");
                                deferred.add(create);
                            }

                        }
                        catch (Throwable e) {
                            throw new ErraiBootstrapFailure("could not initialize extension: " + loadClass.getName(), e);
                        }
                    }
                }
            });


            for (Class bindingType : extensionBindings.keySet()) {
                log.info("added extension binding: " + bindingType.getName());
            }

            log.info("total extension binding: " + extensionBindings.keySet().size());

            visitAllTargets(configRootTargets,
                    new ConfigVisitor() {
                        public void visit(final Class<?> loadClass) {
                            if (loadedComponents.contains(loadClass.getName())) return;


                            if (Module.class.isAssignableFrom(loadClass)) {
                                final Class<? extends Module> clazz = loadClass.asSubclass(Module.class);

                                loadedComponents.add(loadClass.getName());

                                if (clazz.isAnnotationPresent(LoadModule.class)) {
                                    log.info("discovered module : " + clazz.getName() + " -- don't use Modules! Use @Service and MessageCallback!");
                                    Guice.createInjector(new AbstractModule() {
                                        @Override
                                        protected void configure() {
                                            bind(Module.class).to(clazz);
                                            bind(MessageBus.class).toInstance(bus);
                                        }
                                    }).getInstance(Module.class).init();
                                }

                            }
                            else if (loadClass.isAnnotationPresent(Service.class)) {
                                Object svc = null;

                                Class remoteImpl = getRemoteImplementation(loadClass);
                                if (remoteImpl != null) {
                                    createRPCScaffolding(remoteImpl, loadClass, bus);
                                }
                                else if (MessageCallback.class.isAssignableFrom(loadClass)) {
                                    final Class<? extends MessageCallback> clazz = loadClass.asSubclass(MessageCallback.class);

                                    loadedComponents.add(loadClass.getName());

                                    log.info("discovered service: " + clazz.getName());
                                    svc = Guice.createInjector(new AbstractModule() {
                                        @Override
                                        protected void configure() {
                                            bind(MessageCallback.class).to(clazz);
                                            bind(MessageBus.class).toInstance(bus);
                                            bind(RequestDispatcher.class).toInstance(dispatcher);

                                            // Add any extension bindings.
                                            for (Map.Entry<Class, Provider> entry : extensionBindings.entrySet()) {
                                                bind(entry.getKey()).toProvider(entry.getValue());
                                            }
                                        }
                                    }).getInstance(MessageCallback.class);

                                    String svcName = clazz.getAnnotation(Service.class).value();

                                    // If no name is specified, just use the class name as the service
                                    // by default.
                                    if ("".equals(svcName)) {
                                        svcName = clazz.getSimpleName();
                                    }

                                    // Subscribe the service to the bus.
                                    bus.subscribe(svcName, (MessageCallback) svc);

                                    RolesRequiredRule rule = null;
                                    if (clazz.isAnnotationPresent(RequireRoles.class)) {
                                        rule = new RolesRequiredRule(clazz.getAnnotation(RequireRoles.class).value(), bus);
                                    }
                                    else if (clazz.isAnnotationPresent(RequireAuthentication.class)) {
                                        rule = new RolesRequiredRule(new HashSet<Object>(), bus);
                                    }
                                    if (rule != null) {
                                        bus.addRule(svcName, rule);
                                    }
                                }

                                if (svc == null) {
                                    svc = Guice.createInjector(new AbstractModule() {
                                        @Override
                                        protected void configure() {
                                            bind(MessageBus.class).toInstance(bus);
                                            bind(RequestDispatcher.class).toInstance(dispatcher);

                                            // Add any extension bindings.
                                            for (Map.Entry<Class, Provider> entry : extensionBindings.entrySet()) {
                                                bind(entry.getKey()).toProvider(entry.getValue());
                                            }
                                        }
                                    }).getInstance(loadClass);
                                }

                                Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();


                                // we scan for endpoints
                                for (final Method method : loadClass.getDeclaredMethods()) {
                                    if (method.isAnnotationPresent(Endpoint.class)) {
                                        epts.put(method.getName(), method.getReturnType() == Void.class ?
                                                new EndpointCallback(svc, method) :
                                                new ConversationalEndpointCallback(svc, method, bus));
                                    }
                                }

                                if (!epts.isEmpty()) {
                                    bus.subscribe(loadClass.getSimpleName() + ":RPC", new RemoteServiceCallback(epts));
                                }

                            }
                            else if (loadClass.isAnnotationPresent(ExposeEntity.class)) {
                                log.info("Marked " + loadClass + " as serializable.");
                                loadedComponents.add(loadClass.getName());
                                serializableTypes.add(loadClass);
                            }
                        }
                    }
            );
        }
        else {
            log.info("auto-scan disabled.");
        }

        String requireAuthenticationForAll = "errai.require_authentication_for_all";

        if (hasProperty(requireAuthenticationForAll) && "true".equals(getProperty(requireAuthenticationForAll))) {
            log.info("authentication for all requests required, adding rule ... ");
            bus.addRule("AuthorizationService", new RolesRequiredRule(new HashSet<Object>(), bus));
        }

        ConfigUtil.cleanupStartupTempFiles();

        log.info("running deferred configuration tasks ...");
        for (Runnable r : deferred) {
            r.run();
        }

        // configure the JSONEncoder...
        JSONEncoder.setSerializableTypes(serializableTypes);

        log.info("Errai bootstraping complete!");
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

    private void createRPCScaffolding(final Class remoteIface, final Class type, final MessageBus bus) {
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageBus.class).toInstance(bus);
                bind(RequestDispatcher.class).toInstance(dispatcher);

                // Add any extension bindings.
                for (Map.Entry<Class, Provider> entry : extensionBindings.entrySet()) {
                    bind(entry.getKey()).toProvider(entry.getValue());
                }
            }
        });
        
        Object svc = injector.getInstance(type);

        Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();
        for (final Method method : type.getDeclaredMethods()) {
            if (RebindUtils.isMethodInInterface(remoteIface, method)) {
                epts.put(RebindUtils.createCallSignature(method), new ConversationalEndpointCallback(svc, method, bus));
            }
        }

        bus.subscribe(remoteIface.getName() + ":RPC", new RemoteServiceCallback(epts));

        new ProxyProvider() {
            {
                AbstractRemoteCallBuilder.setProxyFactory(this);
            }
            public Object getRemoteProxy(Class proxyType) {
                return new RuntimeException("This API is not supported in the server-side environment.");
            }
        };

    }


    public static interface Creator {
        public void create(Inject injector);
    }

    /**
     * Gets the resource providers associated with this configurator
     *
     * @return the resource providers associated with this configurator
     */
    public Map<String, Provider> getResourceProviders() {
        return this.resourceProviders;
    }

    /**
     * Gets a list of all configuration targets
     *
     * @return list of all configuration targets
     */
    public List<File> getConfigurationRoots() {
        return this.configRootTargets;
    }

    /**
     * Returns true if the configuration has this <tt>key</tt> property
     *
     * @param key - the property too search for
     * @return false if the property does not exist
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Gets the property associated with the key
     *
     * @param key - the key to search for
     * @return the property, if it exists, null otherwise
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Gets the resources attached to the specified resource class
     *
     * @param resourceClass - the class to search the resources for
     * @param <T>           - the class type
     * @return the resource of type <tt>T</tt>
     */
    public <T> T getResource(Class<? extends T> resourceClass) {
        if (extensionBindings.containsKey(resourceClass)) {
            return (T) extensionBindings.get(resourceClass).get();
        }
        else {
            return null;
        }
    }

    /**
     * Gets all serializable types
     *
     * @return all serializable types
     */
    public Set<Class> getAllSerializableTypes() {
        return serializableTypes;
    }

    /**
     * Gets the configured dispatcher, which is used to deliver the messages
     *
     * @return the configured dispatcher
     */
    public RequestDispatcher getConfiguredDispatcher() {
        return dispatcher;
    }

    public SessionProvider getConfiguredSessionProvider() {
        return sessionProvider;
    }
}
