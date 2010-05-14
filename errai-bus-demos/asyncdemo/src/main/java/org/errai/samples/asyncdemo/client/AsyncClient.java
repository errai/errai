package org.errai.samples.asyncdemo.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.protocols.MessageParts;

public class AsyncClient implements EntryPoint {
    public void onModuleLoad() {
        final HorizontalPanel hPanel = new HorizontalPanel();
        final Label messagesReceived = new Label("Messaged Received: ");
        final Label messagesReceivedVal = new Label();

        class Counter {
            int count = 0;

            public void increment() {
                messagesReceivedVal.setText(String.valueOf(++count));
            }
        }

        final Counter counter = new Counter();

        for (int i = 0; i < 5; i++) {
            final VerticalPanel panel = new VerticalPanel();

            final Button startStopButton = new Button("Start" + i);
            final TextBox resultBox = new TextBox();
            resultBox.setEnabled(false);

            final String receiverName = "RandomNumberReceiver" + i;

            /**
             * Create a callback receiver to receive the data from the server.
             */
            final MessageCallback receiver = new MessageCallback() {
                public void callback(Message message) {
                    counter.increment();
                    Double value = message.get(Double.class, "Data");
                    resultBox.setText(String.valueOf(value));
                }
            };

            /**
             * Subscribe to the receiver using the recevierName.
             */
            ErraiBus.get().subscribe(receiverName, receiver);

            final int num = i;

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
                    startStopButton.setText(("Start"+num).equals(startStopButton.getText()) ? "Stop" + num : "Start" + num);
                }
            });

            panel.add(startStopButton);
            panel.add(resultBox);

            hPanel.add(panel);
        }

        final VerticalPanel outerPanel = new VerticalPanel();
        outerPanel.add(hPanel);

        final HorizontalPanel messageCounter = new HorizontalPanel();
        messageCounter.add(messagesReceived);
        messageCounter.add(messagesReceivedVal);

        outerPanel.add(messageCounter);

        RootPanel.get().add(outerPanel);
    }
}
