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
package org.jboss.errai.cdi.client.email;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.ToolBar;
import org.gwt.mosaic.ui.client.layout.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.client.tracker.ServiceAvailability;
import org.jboss.errai.cdi.client.tracker.ServiceTracker;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;
import org.jboss.errai.workspaces.client.framework.Registry;

import javax.inject.Inject;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 10, 2010
 */
@LoadTool(name = "Email", group = "Advanced")
public class EmailClient implements WidgetProvider
{

  private final MessageBus bus = ErraiBus.get();

  private boolean smtpServiceAvailable = false;

  private HTML smtpStatus;
  private Button button;
  private TextBox body;

  ServiceTracker serviceTracker;

  @Inject
  public EmailClient(ServiceTracker serviceTracker)
  {
    this.serviceTracker = serviceTracker;
  }

  public void provideWidget(ProvisioningCallback callback)
  {
    LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    // ----------------

    ToolBar toolBar = new ToolBar();
    smtpStatus = new HTML("Status");

    button = new Button("Send", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        MessageBuilder.createMessage()
            .toSubject("SMTPService")
            .signalling()
            .with("body", body.getText())
            .noErrorHandling().sendNowWith(bus);
      }
    });
    toolBar.add(button);
    toolBar.add(smtpStatus);
    
    panel.add(toolBar);
    // ----------------

    GridLayout gridLayout = new GridLayout(2, 3);
    gridLayout.setHorizontalAlignment(GridLayoutData.ALIGN_LEFT);
    gridLayout.setVerticalAlignment(GridLayoutData.ALIGN_TOP);

    LayoutPanel header = new LayoutPanel(gridLayout);    
    header.add(new Label("From:"));
    header.add(new HTML("errai-demo@google-io.com"));

    header.add(new Label("To:"));
    header.add(new TextBox());

    header.add(new Label("CC:"));
    header.add(new TextBox());

    // ----------------

    panel.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    body = new TextBox();
    panel.add(body, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
    
    //panel.add(smtpStatus);
        
    // register service availability callback
    serviceTracker.addCallback(
        new ServiceAvailability()
        {
          public void status(String subject, boolean available)
          {
            if(subject.equals("SMTPService"))
            {
              smtpServiceAvailable = available;
              updateServiceStatus();
              String status = available ? "available" : "unavailable!";
              MessageBox.alert("Service Tracker", "SMTP service "+status);
            }
          }          
        }
    );

    // initial state. tool is lazy loaded
    smtpServiceAvailable = serviceTracker.isAvailable("SMTPService");
    updateServiceStatus();
 
    callback.onSuccess(panel);
  }

  private void updateServiceStatus()
  {
    String txt = smtpServiceAvailable ? "SMTP available" : "SMTP unavailable";
    smtpStatus.setText(txt);
    button.setEnabled(smtpServiceAvailable);

  }
}
