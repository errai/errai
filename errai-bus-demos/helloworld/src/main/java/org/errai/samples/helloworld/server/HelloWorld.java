package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;

@Service("HelloWorld")
@RequireAuthentication
public class HelloWorld implements MessageCallback {
    public void callback(Message message) {
        System.out.println("Hello, World!");
    }
}
