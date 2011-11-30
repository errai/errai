/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.errai.samples.helloworld.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.bus.client.util.SimpleMessage;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@EntryPoint
@Service
public class HelloWorld extends VerticalPanel implements MessageCallback {
  private Button sayHello;
  private Label label;

  private MessageBus bus;

  @Inject
  public HelloWorld(MessageBus bus) {
    this.bus = bus;
  }

  public void callback(Message message) {
    label.setText(SimpleMessage.get(message));
  }

  @PostConstruct
  public void init() {
    sayHello = new Button("Say Hello!");

    /**
     * Register click handler.
     */
    sayHello.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        MessageBuilder.createMessage()
            .toSubject("HelloWorldService")
            .with(MessageParts.ReplyTo, "HelloWorld")
            .with("TestLong", 1000l)
            .with("TestDouble", 1500.55d)
            .done().sendNowWith(bus);
      }
    });

    label = new Label();

    add(sayHello);
    add(label);

    RootPanel.get().add(this);
  }
}
