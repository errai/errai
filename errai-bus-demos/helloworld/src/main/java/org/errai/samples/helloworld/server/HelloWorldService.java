package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.Conversation;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;
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
