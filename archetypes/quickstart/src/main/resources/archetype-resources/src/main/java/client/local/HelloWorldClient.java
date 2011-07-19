#set($symbol_pound='#')
    #set($symbol_dollar='$')
    #set($symbol_escape='\' )
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
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;

@EntryPoint
public class HelloWorldClient extends VerticalPanel {

  @Inject
  private MessageBus bus;

  @PostConstruct
  public void init() {
    Button button = new Button("hello!");

    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        MessageBuilder.createMessage()
            .toSubject("HelloWorldService")
            .withValue("Hello, There!")
            .done()
            .repliesTo(new MessageCallback() {
              public void callback(Message message) {
                Window.alert("From Server: " + message.get(String.class, MessageParts.Value));
              }
            })
            .sendNowWith(bus);
      }
    });

    add(button);
    RootPanel.get().add(this);
  }
}