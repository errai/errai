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

import java.util.Enumeration;
import java.util.ResourceBundle;


public class ErraiModule extends ServletModule {
    @Override
    protected void configureServlets() {
        ResourceBundle bundle = ResourceBundle.getBundle("ErraiService");
        Enumeration<String> keys = bundle.getKeys();

        String key;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();

            if ("errai.application_context".equals(key)) {
                String appContext = bundle.getString("errai.application_context") + "erraiBus";
                serve(appContext).with(ErraiServletImpl.class);
            }
        }

        bind(MessageBus.class).to(ServerMessageBusImpl.class);
        bind(ServerMessageBus.class).to(ServerMessageBusImpl.class); 
        bind(ErraiService.class).to(ErraiServiceImpl.class);
        bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
    }

}
