package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.WorkerFactory;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.service.ErraiService;

@Singleton
public class AsyncDispatcher implements RequestDispatcher {
    private WorkerFactory workerFactory;
    private ErraiService service;

    @Inject
    public AsyncDispatcher(ErraiService service) {
        this.service = service;
        this.workerFactory = new WorkerFactory(service);
    }

    public void deliver(CommandMessage message) {
        if (message.hasPart(MessageParts.PriorityProcessing)) {
            service.getBus().sendGlobal(message);
        } else {
            workerFactory.deliver(message);
        }
    }
}
