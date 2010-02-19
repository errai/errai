package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

@Service("HelloWorld")
public class HelloWorld implements MessageCallback {
    private MessageBus bus;

    @Inject
    public HelloWorld(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(Message message) {
        MessageBuilder.createConversation(message)
                .subjectProvided().signalling()
                .with("Text", "Hello, World")
                .noErrorHandling().sendNowWith(bus);
    }
}
