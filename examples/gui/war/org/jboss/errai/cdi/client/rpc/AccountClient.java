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
package org.jboss.errai.cdi.client.rpc;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.tools.source.client.ViewSource;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 */
@LoadTool(name = "RPC Endpoints", group="Examples")
public class AccountClient implements WidgetProvider
{
    private final MessageBus bus = ErraiBus.get();

    private HTML responsePanel;

    public void provideWidget(ProvisioningCallback callback)
    {      
      setupListener();

      LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

      // demo, view source
      ViewSource.on(panel,
          new String[] {"org/jboss/errai/cdi/client/rpc/AccountClient.java",
          "org/jboss/errai/cdi/server/rpc/AccountManagerBean.java"}
      );
      
      Button button = new Button("Load accounts", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          AccountManager call = MessageBuilder.createCall(
              new RemoteCallback<List<Account>>()
              {
                public void callback(List<Account> accounts)
                {
                  StringBuffer sb = new StringBuffer();
                  sb.append("<ul>");
                  for(Account a : accounts)
                    sb.append("<li>").append(a.getId());
                  sb.append("</ul>");
                  responsePanel.setHTML(sb.toString());
                }
              }, AccountManager.class
          );
          
          call.getAllAccounts();
        }
      });

      responsePanel = new HTML();

      panel.add(new HTML("This example shows how to use expose beans through an RPC interface."));
      panel.add(new HTML("(Load account entities from RPC service)"));
      panel.add(button);
      panel.add(responsePanel);

      callback.onSuccess(panel);
    }

    private void setupListener()
    {
    }

}
