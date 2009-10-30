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
