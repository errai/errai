package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

@Service("HelloWorld")
//@RequireAuthentication
public class HelloWorld implements MessageCallback {
    public void callback(Message message) {
        System.out.println("Hello, World!");
    }
}
