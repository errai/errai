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

package org.jboss.errai.tools.monitoring;

import org.jboss.errai.bus.client.framework.MessageBus;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMonitorGUI extends JFrame {
    public static final String APPLICATION_NAME = "ErraiBus Monitor";

    private JTabbedPane tabbedPane1;
    private ServerMonitorPanel serverMonitorPanel;
    private Map<Object, ServerMonitorPanel> remoteBuses;
    private MessageBus serverBus;
    private DataStore dataStore;

    public MainMonitorGUI(MessageBus serverBus) {
        this.serverBus = serverBus;
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        setTitle(APPLICATION_NAME);
        getContentPane().add(tabbedPane1);

        serverMonitorPanel = new ServerMonitorPanel(this, serverBus, "Server");
        tabbedPane1.add("Server", serverMonitorPanel.getPanel());

        remoteBuses = new HashMap<Object, ServerMonitorPanel>();

        setMinimumSize(new Dimension(600, 500));
        setSize(600, 500);
        setLocation(150, 150);

        dataStore = new DataStore();

        setVisible(true);
    }

    public ServerMonitorPanel getServerMonitorPanel() {
        return serverMonitorPanel;
    }

    public void attachRemoteBus(Object id) {
        if (remoteBuses.containsKey(id)) {
           return;
        }
        ServerMonitorPanel newServerMonitor = new ServerMonitorPanel(this, new ClientBusProxyImpl(serverBus), String.valueOf(id));
        remoteBuses.put(id, newServerMonitor);

        tabbedPane1.add(String.valueOf(id), newServerMonitor.getPanel());
    }
    
    public ServerMonitorPanel getRemoteBus(Object id) {
        return remoteBuses.get(id);
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public static void main(String[] args) {
        MainMonitorGUI monitor = new MainMonitorGUI(new ClientBusProxyImpl(null));
        monitor.getServerMonitorPanel().addServiceName("Foo");
    }
}
