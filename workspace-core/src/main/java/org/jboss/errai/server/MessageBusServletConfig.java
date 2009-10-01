package org.jboss.errai.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

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
            }
        });
    }
}
