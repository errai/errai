package org.jboss.errai.bus.server.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.HttpSessionProvider;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.SessionProvider;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;

import javax.servlet.http.HttpServlet;

public abstract class AbstractErraiServlet extends HttpServlet {
    protected ErraiService service =
            Guice.createInjector(new AbstractModule() {
                public void configure() {
                    bind(MessageBus.class).to(ServerMessageBusImpl.class);
                    bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
                    bind(ErraiService.class).to(ErraiServiceImpl.class);
                    bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
                }
            }).getInstance(ErraiService.class);

    protected SessionProvider sessionProvider = new HttpSessionProvider();
}
