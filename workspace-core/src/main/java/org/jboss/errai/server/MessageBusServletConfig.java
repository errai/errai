package org.jboss.errai.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.jboss.errai.client.security.AuthenticationHandler;
import org.jboss.errai.server.bus.MessageBus;
import org.jboss.errai.server.bus.MessageBusImpl;
import org.jboss.errai.server.bus.MessageBusProvider;
import org.jboss.errai.server.security.auth.AuthorizationAdapter;
import org.jboss.errai.server.security.auth.JAASAdapter;
import org.jboss.errai.server.service.ErraiService;
import org.jboss.errai.server.service.ErraiServiceImpl;

import java.util.ResourceBundle;

public class MessageBusServletConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                ResourceBundle bundle = ResourceBundle.getBundle("errai");
                String appContext =  bundle.getString("errai.application_context") + "erraiBus";

                serve(appContext).with(MessageBusServiceImpl.class);

                
                bind(MessageBus.class).to(MessageBusImpl.class);
                bind(ErraiService.class).to(ErraiServiceImpl.class);
                bind(AuthorizationAdapter.class).to(JAASAdapter.class);     
            }
        });
    }
}
