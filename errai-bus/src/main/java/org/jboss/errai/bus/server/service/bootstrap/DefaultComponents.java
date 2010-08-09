/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.bus.server.service.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.ModelAdapter;
import org.jboss.errai.bus.client.framework.NoopModelAdapter;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.HttpSessionProvider;
import org.jboss.errai.bus.server.SimpleDispatcher;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.io.JSONMessageServer;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.Guice.createInjector;

/**
 * Load the default components configured through ErraiService.properties.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
class DefaultComponents implements BootstrapExecution {
    private Logger log = LoggerFactory.getLogger(DefaultComponents.class);

    public void execute(final BootstrapContext context) {

        final ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl) context.getConfig();
        final NoopModelAdapter adapter = new NoopModelAdapter();

        final ResourceProvider<ModelAdapter> modelAdapterProvider = new ResourceProvider<ModelAdapter>() {
            public ModelAdapter get() {
                return adapter;
            }
        };

        /*** ModelAdapter ***/
        config.getExtensionBindings().put(ModelAdapter.class, modelAdapterProvider);

        MessageBuilder.setMessageProvider(JSONMessageServer.PROVIDER);

        /*** Authentication Adapter ***/

        if (config.hasProperty("errai.authentication_adapter")) {
            try {
                final Class<? extends AuthenticationAdapter> authAdapterClass = Class.forName(config.getProperty("errai.authentication_adapter"))
                        .asSubclass(AuthenticationAdapter.class);

                log.info("authentication adapter configured: " + authAdapterClass.getName());

                final Runnable create = new Runnable() {
                    public void run() {
                        final AuthenticationAdapter authAdapterInst = Guice.createInjector(new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(AuthenticationAdapter.class).to(authAdapterClass);
                                bind(ErraiServiceConfigurator.class).toInstance(context.getConfig());
                                bind(MessageBus.class).toInstance(context.getBus());
                                bind(ServerMessageBus.class).toInstance(context.getBus());
                            }
                        }).getInstance(AuthenticationAdapter.class);

                        config.getExtensionBindings().put(AuthenticationAdapter.class, new ResourceProvider() {
                            public Object get() {
                                return authAdapterInst;
                            }
                        });
                    }
                };

                try {
                    create.run();
                }
                catch (Throwable e) {
                    log.info("authentication adapter " + authAdapterClass.getName() + " cannot be bound yet, deferring ...");
                    context.defer(create);
                }

            }
            catch (ErraiBootstrapFailure e) {
                throw e;
            }
            catch (Exception e) {
                throw new ErraiBootstrapFailure("cannot configure authentication adapter", e);
            }
        }


        /*** Dispatcher ***/

        RequestDispatcher dispatcher = createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Class<? extends RequestDispatcher> dispatcherImplementation = SimpleDispatcher.class;

                if (config.hasProperty(ErraiServiceConfigurator.ERRAI_DISPATCHER_IMPLEMENTATION)) {
                    try {
                        dispatcherImplementation = Class.forName(config.getProperty(ErraiServiceConfigurator.ERRAI_DISPATCHER_IMPLEMENTATION))
                                .asSubclass(RequestDispatcher.class);
                    }
                    catch (Exception e) {
                        throw new ErraiBootstrapFailure("could not load request dispatcher implementation class", e);
                    }
                }

                log.info("using dispatcher implementation: " + dispatcherImplementation.getName());

                bind(RequestDispatcher.class).to(dispatcherImplementation);
                bind(ErraiService.class).toInstance(context.getService());
                bind(MessageBus.class).toInstance(context.getBus());
                bind(ErraiServiceConfigurator.class).toInstance(config);
            }
        }).getInstance(RequestDispatcher.class);

        context.getService().setDispatcher(dispatcher);

        /*** Session Provider ***/

        SessionProvider sessionProvider = createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Class<? extends SessionProvider> sessionProviderImplementation = HttpSessionProvider.class;

                if (config.hasProperty(ErraiServiceConfigurator.ERRAI_SESSION_PROVIDER_IMPLEMENTATION)) {
                    try {
                        sessionProviderImplementation = Class.forName(config.getProperty(ErraiServiceConfigurator.ERRAI_SESSION_PROVIDER_IMPLEMENTATION))
                                .asSubclass(SessionProvider.class);
                    }
                    catch (Exception e) {
                        throw new ErraiBootstrapFailure("could not load session provider implementation class", e);
                    }
                }

                log.info("using session provider implementation: " + sessionProviderImplementation.getName());

                bind(SessionProvider.class).to(sessionProviderImplementation);
                bind(ErraiService.class).toInstance(context.getService());
                bind(MessageBus.class).toInstance(context.getBus());
                bind(ErraiServiceConfigurator.class).toInstance(config);
            }
        }).getInstance(SessionProvider.class);

        context.getService().setSessionProvider(sessionProvider);
    }
}
