/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.errai.samples.rpcdemo.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@EntryPoint
public class RPCDemo {
    /**
     * Get an instance of the MessageBus
     */
    private MessageBus bus;

    @Inject
    public RPCDemo(MessageBus bus) {
        this.bus = bus;
    }

    @PostConstruct
    public void init() {
        final Button checkMemoryButton = new Button("Check Memory Free");
        final Label memoryFreeLabel = new Label();

        final TextBox inputOne = new TextBox();
        final TextBox inputTwo = new TextBox();
        final Button appendTwoStrings = new Button("Append");
        final Label appendResult = new Label();

        checkMemoryButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createCall(new RemoteCallback<Long>() {
                    public void callback(Long response) {
                        memoryFreeLabel.setText("Free Memory: " + response);
                    }
                }, TestService.class).getMemoryFree();
            }
        });

        appendTwoStrings.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createCall(new RemoteCallback<String>() {
                    public void callback(String response) {
                        appendResult.setText(response);
                    }
                }, TestService.class).append(inputOne.getText(), inputTwo.getText());
            }
        });

        final Button voidReturn = new Button("Test Add", new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createCall(new RemoteCallback<Long>() {
                    public void callback(Long response) {
                        appendResult.setText(String.valueOf(response));
                    }
                }, TestService.class).add(Long.parseLong(inputOne.getText()), Long.parseLong(inputTwo.getText()));
            }
        });

        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel memoryFreeTest = new HorizontalPanel();
        memoryFreeTest.add(checkMemoryButton);
        memoryFreeTest.add(memoryFreeLabel);
        vPanel.add(memoryFreeTest);

        HorizontalPanel appendTest = new HorizontalPanel();
        appendTest.add(inputOne);
        appendTest.add(inputTwo);
        appendTest.add(appendTwoStrings);
        appendTest.add(appendResult);
        vPanel.add(appendTest);

        vPanel.add(voidReturn);

        RootPanel.get().add(vPanel);
    }
}
