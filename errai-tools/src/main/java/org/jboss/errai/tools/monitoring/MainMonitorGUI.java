/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.tools.monitoring;

import org.jboss.errai.bus.client.api.messaging.MessageBus;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;

public class MainMonitorGUI extends JFrame implements Attachable {
  public static final String APPLICATION_NAME = "Errai Bus Monitor";

  private JTabbedPane tabbedPane1;
  private ServerMonitorPanel serverMonitorPanel;
  private Map<Object, ServerMonitorPanel> remoteBuses;
  private MessageBus serverBus;

  private Dataservice dataStore;

  private ActivityProcessor processor;

  public MainMonitorGUI(Dataservice service, MessageBus serverBus) {
    this.serverBus = serverBus;
    this.dataStore = service;

    tabbedPane1 = new JTabbedPane();

    setTitle(APPLICATION_NAME);

    getContentPane().add(tabbedPane1);
    pack();

    serverMonitorPanel = new ServerMonitorPanel(this, serverBus, "Server");
    tabbedPane1.add("Server", serverMonitorPanel.getPanel());
    remoteBuses = new HashMap<Object, ServerMonitorPanel>();

    setMinimumSize(new Dimension(600, 500));
    setSize(600, 500);
    setLocation(150, 150);
  }

  public ServerMonitorPanel getServerMonitorPanel() {
    return serverMonitorPanel;
  }

  public void attachRemoteBus(Object id) {
    if (remoteBuses.containsKey(id)) {
      return;
    }

    String dispId = String.valueOf(id);

    if (dispId.length() > 16) {
      dispId = dispId.substring(dispId.length() - 17, dispId.length() - 1);
    }
    
    ServerMonitorPanel newServerMonitor = new ServerMonitorPanel(this, new ClientBusProxyImpl(serverBus), valueOf(id));
    newServerMonitor.attach(processor);

    remoteBuses.put(id, newServerMonitor);

    tabbedPane1.add(dispId, newServerMonitor.getPanel());
  }

  public ServerMonitorPanel getBus(Object id) {
    return "Server".equals(id) ? serverMonitorPanel : remoteBuses.get(id);
  }

  public Dataservice getDataStore() {
    return dataStore;
  }

  public void attach(ActivityProcessor proc) {
    this.processor = proc;

    proc.registerEvent(EventType.BUS_EVENT, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        switch (event.getSubType()) {
          case REMOTE_ATTACHED:
            attachRemoteBus(event.getFromBus());
            break;
          case SERVER_SUBSCRIBE:
          case REMOTE_SUBSCRIBE:
            if (!"Server".equals(event.getFromBus()) && !remoteBuses.containsKey(event.getFromBus())) {
              return;
            }

            getBus(event.getFromBus()).addServiceName(event.getSubject());
            break;
          case SERVER_UNSUBSCRIBE:
          case REMOTE_UNSUBSCRIBE:
            ServerMonitorPanel panel = getBus(event.getFromBus());
            if (panel != null) {
              panel.removeServiceName(event.getSubject());
            }
            break;
        }
      }
    });

    serverMonitorPanel.attach(proc);
  }
}
