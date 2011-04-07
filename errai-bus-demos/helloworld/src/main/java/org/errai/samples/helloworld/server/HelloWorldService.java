package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.base.Conversation;
import org.jboss.errai.bus.server.annotations.ApplicationComponent;
import org.jboss.errai.bus.server.annotations.MessageParameter;
import org.jboss.errai.bus.server.annotations.Service;

@ApplicationComponent
public class HelloWorldService  {

    @Service("HelloWorld")
    public void helloWorld(@MessageParameter String val, Conversation conversation) {
        System.out.println("received message: " + val);

        conversation.setValue(val);
        conversation.reply();
    }
}
