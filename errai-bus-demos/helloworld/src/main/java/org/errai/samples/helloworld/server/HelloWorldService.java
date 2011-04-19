package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
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

@Service
public class HelloWorldService implements MessageCallback {
    public void callback(Message message) {
        MessageBuilder.createConversation(message)
                .subjectProvided()
                .withValue("Hello, World!")
                .done().reply();
    }
}
