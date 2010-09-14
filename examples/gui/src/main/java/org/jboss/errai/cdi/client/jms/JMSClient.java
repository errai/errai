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
package org.jboss.errai.cdi.client.jms;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.tools.source.client.ViewSource;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 16, 2010
 */
//@LoadTool(name = "JMS Client", group = "Examples")
public class JMSClient implements WidgetProvider
{
  MessageBus bus = ErraiBus.get();
  private static final String RECV_TOPIC = "jms:topic/inboundTopic";
  private static final String SEND_TOPIC = "jms:topic/outboundTopic";

  public void provideWidget(ProvisioningCallback callback)
  {
    LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    // demo, view source
      ViewSource.on(panel,
          new String[] {"org/jboss/errai/cdi/client/jms/JMSClient.java"}
      );

    final TextBox recv = new TextBox();
    final TextBox send = new TextBox();

    panel.add(new HTML("Messages received from JMS:"));
    panel.add(recv, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    panel.add(new HTML("Message send to JMS:"));
    panel.add(send, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
    panel.add(new Button("Send",
        new ClickHandler()
        {
          public void onClick(ClickEvent clickEvent)
          {
            sendJMSMessage(send);
          }
        })
    );
    send.addKeyDownHandler(new KeyDownHandler()
    {
      public void onKeyDown(KeyDownEvent keyDownEvent)
      {
        if(keyDownEvent.getNativeKeyCode()== KeyCodes.KEY_ENTER)
          sendJMSMessage(send);
      }
    });

    // jms listener
    bus.subscribe(RECV_TOPIC, new MessageCallback()
    {
      public void callback(Message message)
      {
        recv.setText(message.get(String.class, "text"));
      }
    });

    callback.onSuccess(panel);

  }

  private void sendJMSMessage(final TextBox send)
  {
    MessageBuilder.createMessage()
        .toSubject(SEND_TOPIC)
        .signalling()
        .with("text", send.getText())
        .noErrorHandling().sendNowWith(bus);

    send.setText("");
  }
}

