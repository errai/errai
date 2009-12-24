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

package org.jboss.errai.bus.server;

import com.google.inject.servlet.ServletModule;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.jboss.errai.bus.server.servlet.DefaultBlockingServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.Enumeration;
import java.util.ResourceBundle;


public class ErraiModule extends ServletModule {
    public static final String ERRAI_APPLICATION_CONTEXT = "errai.application_context";
    public static final String ERRAI_SERVLET_IMPLEMENTATION = "errai.servlet_implementation";
    public static final String ERRAI_DISPATCHER_IMPLEMENTATION = "errai.dispatcher_implementation";

    private Logger log = LoggerFactory.getLogger("ErraiBootstrap");

    @Override
    protected void configureServlets() {
        ResourceBundle bundle = ResourceBundle.getBundle("ErraiService");
        Enumeration<String> keys = bundle.getKeys();

        String appContext = "/erraiapp/";
        Class<? extends HttpServlet> servletImplementation = DefaultBlockingServlet.class;


        log.info("processing configuration.");


        String key;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();

            if (ERRAI_APPLICATION_CONTEXT.equals(key)) {
                appContext = bundle.getString(ERRAI_APPLICATION_CONTEXT) + "erraiBus";
                //  serve(appContext).with(DefaultBlockingServlet.class);
            } else if (ERRAI_SERVLET_IMPLEMENTATION.equals(key)) {
                try {
                    servletImplementation = Class.forName(bundle.getString(ERRAI_SERVLET_IMPLEMENTATION))
                            .asSubclass(HttpServlet.class);

                    log.info("using servlet implementation: " + servletImplementation.getName());
                }
                catch (Exception e) {
                    throw new ErraiBootstrapFailure("could not load servlet implementation class", e);
                }
            }
        }

        serve(appContext).with(servletImplementation);

        bind(MessageBus.class).to(ServerMessageBusImpl.class);
        bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
        bind(ErraiService.class).to(ErraiServiceImpl.class);
        bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
    }
}
