package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.annotations.Service;


@Service("HelloWorld")
public class HelloWorldService implements MessageCallback {

    private RequestDispatcher dispatcher;

    @Inject
    public HelloWorldService(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void callback(Message message) {
        System.out.println(message.get(String.class, "Message"));

        MessageBuilder.createConversation(message)
                .subjectProvided()
                .copy("Message", message)
                .done().sendNowWith(dispatcher);
    }
}
