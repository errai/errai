package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.util.SimpleMessage;
import org.jboss.errai.bus.server.annotations.ApplicationComponent;
import org.jboss.errai.bus.server.annotations.Service;


@ApplicationComponent
public class HelloWorldService {
    @Service("HelloWorldService")
    public void helloWorld(Message message) {
        SimpleMessage.send(message, "Hello, World");
    }
}
