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
package org.jboss.errai.cdi.test.stress.client.local;

import java.text.ParseException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.test.stress.client.shared.ConfigurationRequest;
import org.jboss.errai.cdi.test.stress.client.shared.SubscriptionRequest;
import org.jboss.errai.cdi.test.stress.client.shared.SubscriptionResponse;
import org.jboss.errai.cdi.test.stress.client.shared.TickEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Client-side entry point to the Errai CDI stress testing application.
 */
@EntryPoint
public class App {

  @Inject
  private Event<SubscriptionRequest> subscriptionEvent;

  @Inject
  private Event<ConfigurationRequest> configurationEvent;

  private final Label initialLatencyLabel = new Label();
  private final DateLabel lastTickTimeLabel = new DateLabel();
  private final Label lastTickIdLabel = new Label();
  private final Label lastTickAgeLabel = new Label();

  private final IntegerBox burstInterval = new IntegerBox();
  private final IntegerBox messagesPerBurst = new IntegerBox();
  private final IntegerBox payloadSize = new IntegerBox();
  
  @PostConstruct
  public void buildUI() {
    Grid confGrid = new Grid(2, 4);
    confGrid.setText(0, 0, "Burst Interval (ms)");
    confGrid.setWidget(1, 0, burstInterval);
    confGrid.setText(0, 1, "Messages Per Burst");
    confGrid.setWidget(1, 1, messagesPerBurst);
    confGrid.setText(0, 2, "Payload Size");
    confGrid.setWidget(1, 2, payloadSize);
    Button setConfigButton = new Button("Set Configuration");
    confGrid.setWidget(1, 3, setConfigButton);
    
    setConfigButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ConfigurationRequest cr = new ConfigurationRequest();
        try {
          cr.setMessageCount(messagesPerBurst.getValueOrThrow());
          cr.setMessageInterval(burstInterval.getValueOrThrow());
          cr.setPayloadSize(payloadSize.getValueOrThrow());
          configurationEvent.fire(cr);
        } catch (ParseException e) {
          Window.alert("The configuration values all have to be whole, positive numbers.");
        }
      }
    });
    RootPanel.get().add(confGrid);
    
    Grid p = new Grid(2, 4);
    p.setCellPadding(5);
    p.setText(0, 0, "Registration Latency (ms)");
    p.setText(0, 1, "Last Tick Timestamp");
    p.setText(0, 2, "Last Tick ID");
    p.setText(0, 3, "Last Tick Age (ms)");
    
    p.setWidget(1, 0, initialLatencyLabel);
    p.setWidget(1, 1, lastTickTimeLabel);
    p.setWidget(1, 2, lastTickIdLabel);
    p.setWidget(1, 3, lastTickAgeLabel);
    RootPanel.get().add(p);

    subscriptionEvent.fire(new SubscriptionRequest(System.currentTimeMillis()));
  }

  public void subscriptionActivated(@Observes SubscriptionResponse response) {
    long offset = response.getServerTime() - System.currentTimeMillis();
    initialLatencyLabel.setText(String.valueOf(offset));
  }

  public void configurationChanged(@Observes ConfigurationRequest cr) {
    burstInterval.setValue(cr.getMessageInterval());
    messagesPerBurst.setValue(cr.getMessageCount());
    payloadSize.setValue(cr.getPayloadSize());
  }
  
  public void tick(@Observes TickEvent tick) {
    long age = System.currentTimeMillis() - tick.getServerTime();
    lastTickTimeLabel.setValue(new Date(tick.getServerTime()));
    lastTickAgeLabel.setText(String.valueOf(age));
    lastTickIdLabel.setText(String.valueOf(tick.getId()));
  }
}