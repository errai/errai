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
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.client.RequestDispatcher;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.Module;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.SimpleDispatcher;
import org.jboss.errai.bus.server.annotations.Endpoint;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.annotations.LoadModule;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;
import org.jboss.errai.bus.server.io.ConversationalEndpointCallback;
import org.jboss.errai.bus.server.io.EndpointCallback;
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
import static org.jboss.errai.bus.server.ErraiModule.ERRAI_DISPATCHER_IMPLEMENTATION;

public class ErraiServiceConfiguratorImpl implements ErraiServiceConfigurator {
    private ServerMessageBus bus;
    private List<File> configRootTargets;
    private Map<String, String> properties;

    private Map<Class, Provider> extensionBindings;
    private Map<String, Provider> resourceProviders;

    private RequestDispatcher dispatcher;

    private ErraiServiceConfigurator configInst = this;

    private Logger log = LoggerFactory.getLogger(ErraiServiceConfigurator.class);

    @Inject
    public ErraiServiceConfiguratorImpl(ServerMessageBus bus) {
        this.bus = bus;
    }

    public void configure(final ErraiService erraiService) {
        properties = new HashMap<String, String>();
        configRootTargets = ConfigUtil.findAllConfigTargets();

        try {
            ResourceBundle erraiServiceConfig = ResourceBundle.getBundle("ErraiService");
            Enumeration<String> keys = erraiServiceConfig.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                properties.put(key, erraiServiceConfig.getString(key));
            }
        }
        catch (Exception e) {
            throw new ErraiBootstrapFailure("error reading from configuration", e);
        }


        // Create a Map to collect any extensions bindings to be bound to
        // services.
        extensionBindings = new HashMap<Class, Provider>();
        resourceProviders = new HashMap<String, Provider>();
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
                        throw new ErraiBootstrapFailure("could not load servlet implementation class", e);
                    }
                }

                log.info("using dispatcher implementation: " + dispatcherImplementation.getName());

                bind(RequestDispatcher.class).to(dispatcherImplementation);
                bind(ErraiService.class).toInstance(erraiService);
                bind(MessageBus.class).toInstance(bus);
                bind(ErraiServiceConfigurator.class).toInstance(configInst);
            }
        }).getInstance(RequestDispatcher.class);

        log.info("beging searching for Errai extensions ...");

        // Search for Errai extensions.
        ConfigUtil.visitAllTargets(configRootTargets, new ConfigVisitor() {
            public void visit(Class<?> loadClass) {
                if (ErraiConfigExtension.class.isAssignableFrom(loadClass)
                        && loadClass.isAnnotationPresent(ExtensionComponent.class)) {

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

        ConfigUtil.visitAllTargets(configRootTargets,
                new ConfigVisitor() {
                    public void visit(final Class<?> loadClass) {
                        if (Module.class.isAssignableFrom(loadClass)) {
                            final Class<? extends Module> clazz = loadClass.asSubclass(Module.class);

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

                        } else if (loadClass.isAnnotationPresent(Service.class)) {
                            Object svc = null;
                            if (MessageCallback.class.isAssignableFrom(loadClass)) {
                                final Class<? extends MessageCallback> clazz = loadClass.asSubclass(MessageCallback.class);
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
                                } else if (clazz.isAnnotationPresent(RequireAuthentication.class)) {
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
                                        bind(Object.class).toInstance(loadClass);
                                        bind(MessageBus.class).toInstance(bus);
                                        bind(RequestDispatcher.class).toInstance(dispatcher);

                                        // Add any extension bindings.
                                        for (Map.Entry<Class, Provider> entry : extensionBindings.entrySet()) {
                                            bind(entry.getKey()).toProvider(entry.getValue());
                                        }
                                    }
                                }).getInstance(Object.class);
                            }

                            Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();

                            // we scan for endpoints
                            for (final Method method : loadClass.getDeclaredMethods()) {
                                if (method.isAnnotationPresent(Endpoint.class)) {
                                    //   Endpoint endpoint = method.getAnnotation(Endpoint.class);
                                    epts.put(method.getName(), method.getReturnType() == Void.class ?
                                            new EndpointCallback(svc, method) :
                                            new ConversationalEndpointCallback(svc, method, bus));

                                    bus.subscribe(loadClass.getSimpleName(), new RemoteServiceCallback(epts));
                                }
                            }
                        }
                    }
                }
        );

        String requireAuthenticationForAll = "errai.require_authentication_for_all";

        if (hasProperty(requireAuthenticationForAll) && "true".equals(getProperty(requireAuthenticationForAll))) {
            log.info("authentication for all requests required, adding rule ... ");
            bus.addRule("ClientNegotiationService", new RolesRequiredRule(new HashSet<Object>(), bus));
        }

        ConfigUtil.cleanupStartupTempFiles();

        log.info("running deferred configuration tasks ...");
        for (Runnable r : deferred) {
            r.run();
        }

        log.info("Errai bootstraping complete!");
    }

    public static interface Creator {
        public void create(Inject injector);
    }

    public Map<String, Provider> getResourceProviders() {
        return this.resourceProviders;
    }

    public List<File> getConfigurationRoots() {
        return this.configRootTargets;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public <T> T getResource(Class<? extends T> resourceClass) {
        return (T) extensionBindings.get(resourceClass).get();
    }

    public RequestDispatcher getConfiguredDispatcher() {
        return dispatcher;
    }
}
