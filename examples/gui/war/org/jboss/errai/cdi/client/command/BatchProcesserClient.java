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
package org.jboss.errai.cdi.client.command;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jul 20, 2010
 */
@LoadTool(name = "Command Execution", group = "Examples")
public class BatchProcesserClient implements MessageCallback, WidgetProvider
{

  private boolean startBatch = true;
  private MessageBus bus;
  private HTML responsePanel;

  @Inject
  public BatchProcesserClient(MessageBus bus)
  {
    this.bus = bus;
  }

  public void callback(Message message)
  {
    responsePanel.setText(message.get(String.class, "response"));
  }

  public void provideWidget(ProvisioningCallback callback)
  {
    LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    final Button btn = new Button("Start");

    ClickHandler clickHandler = new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        if (startBatch)
        {
          MessageBuilder.createMessage()
              .toSubject("BatchProcessor")
              .command("start")
              .with(MessageParts.ReplyTo, "BatchProcesserClient")
              .done().sendNowWith(bus);

          startBatch = false;
        }
        else
        {
          MessageBuilder.createMessage()
              .toSubject("BatchProcessor")
              .command("stop")
              .with(MessageParts.ReplyTo, "BatchProcesserClient")
              .done().sendNowWith(bus);

          startBatch = true;
        }

        String label = startBatch ? "Start" : "Stop";
        btn.setText(label);
      }
    };

    btn.addClickHandler(clickHandler);

    panel.add(btn);
    responsePanel = new HTML();
    panel.add(responsePanel);

    bus.subscribe("BatchProcesserClient", this);
    
    callback.onSuccess(panel);
  }
}
