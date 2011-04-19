package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.api.base.Reply;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.annotations.ApplicationComponent;
import org.jboss.errai.bus.server.annotations.MessageParameter;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueSession;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;

@ApplicationComponent
public class HelloWorldService {

    private MessageBus bus;
    private TaskManager manager;

    @Inject
    public HelloWorldService(final MessageBus bus, final TaskManager manager) {
        this.bus = bus;
        this.manager = manager;

        manager.scheduleRepeating(TimeUnit.SECONDS, 30, new Runnable() {
            public void run() {
                System.out.println("INVALIDATE KINDS!");

                ServerMessageBusImpl serverBus = (ServerMessageBusImpl) bus;

                for (Map.Entry<QueueSession, MessageQueue> entry : serverBus.getMessageQueues().entrySet()) {
                   // entry.getKey().endSession();
                     entry.getValue().stopQueue();
                }
            }
        });

    }

    @Service("HelloWorld")
    public void helloWorld(@MessageParameter String val, Reply reply) {
        System.out.println("received message: " + val);

        reply.setValue(val);
        reply.reply();


    }
}
