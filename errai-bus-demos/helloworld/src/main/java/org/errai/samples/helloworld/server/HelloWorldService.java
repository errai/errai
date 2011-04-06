package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.annotations.Service;


@Service("HelloWorld")
public class HelloWorldService implements MessageCallback {
    public void callback(Message message) {
        System.out.println(message.get(String.class, MessageParts.Value));

        MessageBuilder.createConversation(message)
                .subjectProvided()
                .copy(MessageParts.Value, message)
                .withProvided("Time", new ResourceProvider<Long>() {
                    public Long get() {
                        return System.currentTimeMillis();
                    }
                })
                .done().replyRepeating(TimeUnit.MILLISECONDS, 100);
    }
}
