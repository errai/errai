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
package org.jboss.errai.cdi.client.events;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.tools.source.client.ViewSource;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

@LoadTool(name = "Event integration", group = "Examples")
public class FraudClient extends LayoutPanel {

  @Inject
  public Event event;

  private HTML responsePanel;

  public FraudClient() {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    Button button = new Button("Create activity", new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        event.fire(new AccountActivity());
      }
    });

    responsePanel = new HTML();

    add(button);
    add(responsePanel);
  }


  public void processFraud(@Observes Fraud fraudEvent) {
    responsePanel.setText("Fraud detected: " + fraudEvent.getTimestamp());
  }
}
