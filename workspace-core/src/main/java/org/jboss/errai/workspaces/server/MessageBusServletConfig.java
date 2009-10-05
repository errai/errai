package org.jboss.errai.workspaces.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.jboss.errai.workspaces.server.bus.MessageBus;
import org.jboss.errai.workspaces.server.bus.MessageBusImpl;
import org.jboss.errai.workspaces.server.security.auth.AuthorizationAdapter;
import org.jboss.errai.workspaces.server.security.auth.JAASAdapter;
import org.jboss.errai.workspaces.server.service.ErraiService;
import org.jboss.errai.workspaces.server.service.ErraiServiceImpl;

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
