/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                MessageBuilder.createMessage()
                        .toSubject("BroadcastService").signalling()
                        .with("BroadcastText", inputBox.getText())
                        .noErrorHandling().sendNowWith(bus);
            }
        });

        final Label broadcastReceive = new Label();

        /**
         * Declare a local service to receive messages on the subject
         * "BroadCastReceiver".
         */
        bus.subscribe("BroadcastReceiver", new MessageCallback() {
            public void callback(Message message) {
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
