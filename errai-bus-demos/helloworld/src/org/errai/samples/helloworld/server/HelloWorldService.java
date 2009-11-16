package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;


@Service
public class HelloWorldService implements MessageCallback {
    private MessageBus bus;

    @Inject
    public HelloWorldService(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(CommandMessage message) {
        ConversationMessage.create(message)
                .set("Message", "Hello, World!")
                .sendNowWith(bus);
    }
}
