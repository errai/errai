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

package org.jboss.errai.bus.server.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.util.ErrorHelper;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.service.bootstrap.BootstrapContext;
import org.jboss.errai.bus.server.service.bootstrap.OrderedBootstrap;

/**
 * Default implementation of the ErraiBus server-side service.
 */
@Singleton
public class ErraiServiceImpl<S> implements ErraiService<S> {

    private ServerMessageBus bus;
    private ErraiServiceConfigurator config;

    private SessionProvider<S> sessionProvider;
    private RequestDispatcher dispatcher;
    
    /**
     * Initializes the errai service with a bus and configurator
     *
     * @param bus          - the bus to be associated with this service
     * @param configurator - the configurator to take care of the configuration for the service
     */
    @Inject
    public ErraiServiceImpl(final ServerMessageBus bus,
                            final ErraiServiceConfigurator configurator) {
        this.bus = bus;
        this.config = configurator;



        boostrap();
    }

    private void boostrap() {

        BootstrapContext context = new BootstrapContext(this, bus, config);
        new OrderedBootstrap().execute(context);

    }

    /**
     * Passes off the message to the bus for handling
     *
     * @param message - the message to store/deliver
     */
    public void store(Message message) {
        if (message == null) return;
        
        message.addResources(config.getResourceProviders());

        /**
         * Pass the message off to the messaging bus for handling.
         */
        try {
            getDispatcher().dispatchGlobal(message);
        }
        catch (Throwable t) {
            t.printStackTrace();
            ErrorHelper.sendClientError(bus, message, t.getMessage(), t);
        }
    }


    /**
     * Gets the bus associated with this service
     *
     * @return the bus associated with this service
     */
    public ServerMessageBus getBus() {
        return bus;
    }

    /**
     * Gets the configuration used to initalize the service
     *
     * @return the errai service configurator
     */
    public ErraiServiceConfigurator getConfiguration() {
        return config;
    }

    public SessionProvider<S> getSessionProvider() {
        return sessionProvider;
    }

    public void setSessionProvider(SessionProvider<S> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
}
