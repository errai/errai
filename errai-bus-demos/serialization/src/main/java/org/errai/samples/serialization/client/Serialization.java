package org.errai.samples.serialization.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.errai.samples.serialization.client.model.Record;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.MessageParts;

import java.util.List;

public class Serialization implements EntryPoint {
    private MessageBus bus = ErraiBus.get();

    public void onModuleLoad() {
        final HTML html = new HTML();


        bus.subscribe("MrClient", new MessageCallback() {
            public void callback(CommandMessage message) {
                html.setHTML(message.get(String.class, "Message"));
            }
        });


        ConversationMessage.create()
                .toSubject("ObjectService")
                .set(MessageParts.ReplyTo, "MrClient")
                .sendNowWith(bus);

        RootPanel.get().add(html);
    }
}
