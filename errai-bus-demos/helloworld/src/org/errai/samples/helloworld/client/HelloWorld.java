package org.errai.samples.helloworld.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;

public class HelloWorld implements EntryPoint {
    private MessageBus bus = ErraiBus.get();

    public void onModuleLoad() {
        final HTML html = new HTML();
        bus.conversationWith(CommandMessage.create().toSubject("HelloWorldService"),
                new MessageCallback() {
                    public void callback(CommandMessage message) {
                        String messageText = message.get(String.class, "Message");

                        html.setHTML(messageText);

                    }
                });

        RootPanel.get().add(html);
    }
}
