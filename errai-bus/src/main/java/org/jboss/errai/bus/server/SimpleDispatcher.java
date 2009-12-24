package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.service.ErraiService;

@Singleton
public class SimpleDispatcher implements RequestDispatcher {
    private ErraiService svc;
    private MessageBus bus;
    
    @Inject
    public SimpleDispatcher(ErraiService svc) {
        this.svc = svc;
        this.bus = svc.getBus();
    }

    public void deliver(CommandMessage message) {
        bus.sendGlobal(message);
    }
}
