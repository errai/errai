package org.jboss.errai.bus.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.security.auth.AuthorizationAdapter;
import org.jboss.errai.bus.server.security.auth.DefaultAdapter;
import org.jboss.errai.bus.server.security.auth.JAASAdapter;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;

import java.util.Enumeration;
import java.util.ResourceBundle;

public class MessageBusServletConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                ResourceBundle bundle = ResourceBundle.getBundle("errai");
                Enumeration<String> keys = bundle.getKeys();

                boolean authAdapterSpecified = false;

                String key;
                while (keys.hasMoreElements()) {
                    key = keys.nextElement();

                    if ("errai.application_context".equals(key)) {
                        String appContext = bundle.getString("errai.application_context") + "erraiBus";
                        serve(appContext).with(MessageBusServiceImpl.class);
                    } else if ("errai.authentication_adapter".equals(key)) {
                        try {
                            Class<? extends AuthorizationAdapter> authAdapterClass = Class.forName(bundle.getString(key)).asSubclass(AuthorizationAdapter.class);
                            bind(AuthorizationAdapter.class).to(authAdapterClass);
                            authAdapterSpecified = true;

                        }
                        catch (Exception e) {
                            throw new RuntimeException("Could not load authentication adapter: "
                                    + bundle.getString(key), e);
                        }

                    }
                }

                bind(MessageBus.class).to(MessageBusImpl.class);
                bind(ServerMessageBus.class).to(MessageBusImpl.class);
                bind(ErraiService.class).to(ErraiServiceImpl.class);

                if (!authAdapterSpecified) {
                    bind(AuthorizationAdapter.class).to(DefaultAdapter.class);
                }


            }
        });
    }
}
