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
package org.jboss.errai.cdi.client.service;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.cdi.api.Conversation;
import org.jboss.errai.tools.source.client.ViewSource;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

import javax.inject.Inject;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 */
@LoadTool(name = "Beans as Services", group = "Examples")
public class CalculatorClient implements WidgetProvider {
    private final MessageBus bus;

    private HTML responsePanel;

    @Service("calculationResult")
    public MessageCallback responseListener = new MessageCallback() {
        public void callback(Message message) {
            responsePanel.setText("Calculation result: " + message.get(Double.class, "result") + "");
        }
    };

    @Inject
    public CalculatorClient(MessageBus bus) {
        this.bus = bus;
    }

    public void provideWidget(ProvisioningCallback callback) {
        LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

        // demo, view source
        ViewSource.on(panel,
                new String[]{"org/jboss/errai/cdi/client/service/CalculatorClient.java",
                        "org/jboss/errai/cdi/server/service/CalculatorService.java"}
        );

        final TextBox a = new TextBox();
        final TextBox b = new TextBox();

        LayoutPanel inputPanel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
        inputPanel.add(a);
        inputPanel.add(b);

        Button button = new Button("Send to service", new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                if (validate(a, b)) {
                    MessageBuilder.createMessage()
                            .toSubject("calculator")
                            .signalling()
                            .with(MessageParts.ReplyTo, "calculationResult")
                            .with("a", Double.valueOf(a.getText()))
                            .with("b", Double.valueOf(b.getText()))
                            .noErrorHandling()
                            .sendNowWith(bus);
                }
            }
        });

        responsePanel = new HTML();

        panel.add(new HTML("This example shows how to use managed beans as services."));
        panel.add(new HTML("(Add to numbers)"));
        panel.add(inputPanel);
        panel.add(button);
        panel.add(responsePanel);

        callback.onSuccess(panel);
    }

    private boolean validate(TextBox... input) {
        boolean valid = true;
        for (TextBox t : input) {
            if (t.getText() == null || t.getText().equals("")) {
                valid = false;
                MessageBox.error("Input Validation", "Please enter valid numbers");
            }
        }

        return valid;
    }

}

