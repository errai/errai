package org.errai.samples.asyncdemo.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.protocols.MessageParts;

public class AsyncClient implements EntryPoint {
    public void onModuleLoad() {
        final Button startStopButton = new Button("Start");
        final TextBox resultBox = new TextBox();
        resultBox.setEnabled(false);

        final String receiverName = "RandomNumberReceiver";

        /**
         * Create a callback receiver to receive the data from the server.
         */
        final MessageCallback receiver = new MessageCallback() {
            public void callback(Message message) {
                Double value = message.get(Double.class, "Data");
                resultBox.setText(String.valueOf(value));
            }
        };

        /**
         * Subscribe to the receiver using the recevierName.
         */
        ErraiBus.get().subscribe(receiverName, receiver);

        startStopButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                /**
                 * Send a message to Start/Stop the task on the server.
                 */
                MessageBuilder.createMessage()
                        .toSubject("AsyncService")
                        .command(startStopButton.getText())
                        .with(MessageParts.ReplyTo, receiverName)
                        .noErrorHandling().sendNowWith(ErraiBus.get());

                /**
                 * Flip-flop the value of the button every time it's pushed between 'Start' and 'Stop'
                 */
                startStopButton.setText("Start".equals(startStopButton.getText()) ? "Stop" : "Start");
            }
        });

        VerticalPanel panel = new VerticalPanel();
        panel.add(startStopButton);
        panel.add(resultBox);
        
        RootPanel.get().add(panel);
    }
}
