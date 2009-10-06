package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.Module;
import org.jboss.errai.bus.server.annotations.LoadModule;

@LoadModule
public class HelloWorld implements Module {
    private MessageBus bus;

    @Inject
    public HelloWorld(MessageBus bus) {
        this.bus = bus;
    }

    public void init() {
        bus.subscribe("HelloWorld", new MessageCallback() {
            public void callback(CommandMessage message) {
                System.out.println("Hello, World!");
            }
        });
    }
}
