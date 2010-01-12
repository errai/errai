package org.jboss.errai.bus.server.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.apache.catalina.CometEvent;
import org.apache.catalina.CometProcessor;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.jboss.servlet.http.HttpEvent;
import org.jboss.servlet.http.HttpEventServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErraiAutoServlet extends HttpServlet implements CometProcessor {
    private HttpServlet delegate;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void init() throws ServletException {
        // bypass guice-servlet
        ErraiService service = Guice.createInjector(new AbstractModule() {
            public void configure() {
                bind(MessageBus.class).to(ServerMessageBusImpl.class);
                bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
                bind(ErraiService.class).to(ErraiServiceImpl.class);
                bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
            }
        }).getInstance(ErraiService.class);

        log.info("determining environment...");

        if (delegate == null) {
            try {
                delegate = new JettyContinuationsServlet(service);
            }
            catch (Throwable e) {
            }
        }

//        if (delegate == null) {
//            try {
//                delegate = new JBossCometServlet(service);
//            }
//            catch (Throwable e) {
//            }
//        }

        if (delegate == null) {
            try {
                delegate = new TomcatCometServlet(service);
            }
            catch (Throwable e) {
            }
        }

        if (delegate == null) {
            delegate = new DefaultBlockingServlet(service);
        }

        log.info("Using servlet: " + delegate.getClass().getName());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        delegate.service(req, resp);
    }

    @Override
    public void event(CometEvent cometEvent) throws IOException, ServletException {
        ((CometProcessor) delegate).event(cometEvent);
    }

//    @Override
//    public void event(HttpEvent httpEvent) throws IOException, ServletException {
//        System.out.println("Event:" + httpEvent);
//        ((HttpEventServlet) delegate).event(httpEvent);
//    }
}
