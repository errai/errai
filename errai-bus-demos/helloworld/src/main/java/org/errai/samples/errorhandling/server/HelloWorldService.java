package org.errai.samples.errorhandling.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

@Service("HelloWorldService")
public class HelloWorldService implements MessageCallback {
    private MessageBus bus;

    @Inject
    public HelloWorldService(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(Message message) {
      System.out.println("Received: "+message.get(String.class, "msg"));
    }
}
