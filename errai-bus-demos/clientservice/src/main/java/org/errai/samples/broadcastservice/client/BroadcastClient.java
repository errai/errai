package org.errai.samples.broadcastservice.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.*;

public class BroadcastClient implements EntryPoint {
    private MessageBus bus = ErraiBus.get();

    public void onModuleLoad() {
        final VerticalPanel panel = new VerticalPanel();
        final TextBox inputBox = new TextBox();
        final Button sendBroadcast = new Button("Broadcast!");
        
        sendBroadcast.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                /**
                 * Send a message to the BroadcastService with the contents of the
                 * inputBox as the "BroadcastText" field.
                 */
                CommandMessage.create()
                        .toSubject("BroadcastService")
                        .set("BroadcastText", inputBox.getText())
                        .sendNowWith(bus);
            }
        });

        final Label broadcastReceive = new Label();

        /**
         * Declare a local service to receive messages on the subject
         * "BroadCastReceiver".
         */
        bus.subscribe("BroadcastReceiver", new MessageCallback() {
            public void callback(CommandMessage message) {
                /**
                 * When a message arrives, extract the "BroadcastText" field and
                 * update the broadcastReceive Label widget with the contents.
                 */
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
