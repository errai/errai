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
package ${package}.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import ${package}.client.shared.MessageEvent;
import ${package}.client.shared.ResponseEvent;

/**
 * Main application entry point.
 */
@EntryPoint
public class App {
    @Inject
    private Event<MessageEvent> messageEvent;

    private final Label responseLabel = new Label();

    @PostConstruct
    public void buildUI() {
        final Button button = new Button("Send");
        final TextBox message = new TextBox();

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                messageEvent.fire(new MessageEvent(message.getText()));
            }
        });

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.add(message);
        horizontalPanel.add(button);
        horizontalPanel.add(responseLabel);

        RootPanel.get().add(horizontalPanel);
    }

    public void response(@Observes ResponseEvent event) {
        responseLabel.setText("Message from Server: " + event.getMessage().toUpperCase());
    }
}