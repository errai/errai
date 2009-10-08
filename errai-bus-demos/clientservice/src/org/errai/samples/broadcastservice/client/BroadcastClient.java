package org.errai.samples.broadcastservice.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ErraiClient;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;

public class BroadcastClient implements EntryPoint {
    private MessageBus bus = ErraiClient.getBus();

    public void onModuleLoad() {
        final VerticalPanel panel = new VerticalPanel();

        final TextBox inputBox = new TextBox();
        final Button sendBroadcast = new Button("Broadcast!");
        
        sendBroadcast.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                CommandMessage.create()
                        .toSubject("BroadcastService")
                        .set("BroadcastText", inputBox.getText())
                        .sendNowWith(bus);
            }
        });

        final Label broadcastReceive = new Label();

        bus.subscribe("BroadcastReceiver", new MessageCallback() {
            public void callback(CommandMessage message) {
                String broadcastText = message.get(String.class, "BroadcastText");
                broadcastReceive.setText(broadcastText);
            }
        });

        panel.add(inputBox);
        panel.add(sendBroadcast);
        panel.add(broadcastReceive);

        RootPanel.get().add(panel);
    }
}
