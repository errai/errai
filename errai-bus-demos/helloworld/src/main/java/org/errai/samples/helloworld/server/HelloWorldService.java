package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.base.Reply;
import org.jboss.errai.bus.server.annotations.ApplicationComponent;
import org.jboss.errai.bus.server.annotations.MessageParameter;
import org.jboss.errai.bus.server.annotations.Service;

@ApplicationComponent
public class HelloWorldService  {

    @Service("HelloWorld")
    public void helloWorld(@MessageParameter String val, Reply reply) {
        System.out.println("received message: " + val);

        reply.setValue(val);
        reply.reply();
    }
}
