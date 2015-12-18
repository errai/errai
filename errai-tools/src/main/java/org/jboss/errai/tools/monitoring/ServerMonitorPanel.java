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

import static java.lang.String.valueOf;
import static javax.swing.SwingUtilities.invokeLater;
import static org.jboss.errai.tools.monitoring.UiHelper.getSwIcon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.util.BusTools;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.mvel2.util.StringAppender;

public class ServerMonitorPanel implements Attachable {
  private MainMonitorGUI mainMonitorGUI;
  private MessageBus messageBus;
  private String busId;

  private JList busServices;
  private JTree serviceExplorer;

  private final DefaultListModel busServicesModel;

  private JPanel rootPanel;

  private String currentlySelectedService;

  private ServerLogPanel logPanel;

  private ActivityProcessor processor;

  private Map<String, ServiceActivityMonitor> monitors = new HashMap<String, ServiceActivityMonitor>();

  public ServerMonitorPanel(MainMonitorGUI gui, MessageBus bus, String busId) {
    this.mainMonitorGUI = gui;
    this.messageBus = bus;
    this.busId = busId;

    rootPanel = new JPanel();
    rootPanel.setLayout(new BorderLayout());

    JButton activityConsoleButton = new JButton("Activity Console");
    JButton monitorButton = new JButton("Monitor Service...");
    JButton conversationsButton = new JButton("Conversations ...");

    busServices = new JList();
    busServices.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    busServices.setModel(busServicesModel = new DefaultListModel());
    busServices.setCellRenderer(new ServicesListCellRender());

    serviceExplorer = new JTree();

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(busServices), new JScrollPane(
            serviceExplorer));
    splitPane.setDividerLocation(150);

    rootPanel.add(splitPane, BorderLayout.CENTER);

    JPanel southPanel = new JPanel();
    southPanel.setLayout(new BorderLayout());
    rootPanel.add(southPanel, BorderLayout.SOUTH);

    southPanel.add(activityConsoleButton, BorderLayout.WEST);

    JPanel southEastPanel = new JPanel();
    southEastPanel.setLayout(new FlowLayout());
    southEastPanel.add(conversationsButton);
    southEastPanel.add(monitorButton);

    southPanel.add(southEastPanel, BorderLayout.EAST);

    busServices.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        currentlySelectedService = getCurrentServiceSelection();
        generateServiceExplorer();
      }
    });

    busServices.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
          return;
        openActivityMonitor();
      }
    });

    monitorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openActivityMonitor();
      }
    });

    conversationsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openConversationMonitor();
      }
    });

    activityConsoleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openServerLog();
      }
    });

    DefaultTreeModel model = (DefaultTreeModel) serviceExplorer.getModel();
    ((DefaultMutableTreeNode) serviceExplorer.getModel().getRoot()).removeAllChildren();
    serviceExplorer.setRootVisible(false);

    serviceExplorer.setCellRenderer(new MonitorTreeCellRenderer());

    serviceExplorer.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

    model.reload();
  }

  public void attach(ActivityProcessor proc) {
    this.processor = proc;
  }

  private void openActivityMonitor() {
    if (monitors.containsKey(getCurrentServiceSelection())) {
      monitors.get(currentlySelectedService).toFront();
    }
    else {
      ServiceActivityMonitor sam = new ServiceActivityMonitor(this, busId, currentlySelectedService);
      sam.attach(processor);
      monitors.put(currentlySelectedService, sam);
    }
  }

  public void openConversationMonitor() {
    String key = currentlySelectedService + ":Conversations";

    if (monitors.containsKey(key)) {
      monitors.get(key).toFront();
    }
    else {
      ServiceActivityMonitor sam = new ConversationActivityMonitor(this, busId, currentlySelectedService);
      sam.attach(processor);
      monitors.put(key, sam);
    }
  }

  private void openServerLog() {
    if (this.logPanel != null && this.logPanel.isVisible()) {
      return;
    }
    this.logPanel = new ServerLogPanel(mainMonitorGUI);
    this.logPanel.attach(processor);
  }

  void stopMonitor(String service) {
    monitors.remove(service);
  }

  private String getCurrentServiceSelection() {
    return valueOf(busServicesModel.get(busServices.getSelectedIndex()));
  }

  public void addServiceName(final String serviceName) {
    synchronized (busServicesModel) {
      if (busServicesModel.contains(serviceName))
        return;

      invokeLater(new Runnable() {
        public void run() {
          busServicesModel.addElement(serviceName);
        }
      });
    }
  }

  public void removeServiceName(final String serviceName) {
    synchronized (busServicesModel) {
      if (!busServicesModel.contains(serviceName))
        return;

      invokeLater(new Runnable() {
        public void run() {
          busServicesModel.removeElement(serviceName);
        }
      });
    }
  }

  public JPanel getPanel() {
    return rootPanel;
  }

  private void generateServiceExplorer() {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) serviceExplorer.getModel().getRoot();

    node.setUserObject(new JLabel(currentlySelectedService
            + (BusTools.isReservedName(currentlySelectedService) ? " (Built-in)" : ""), getSwIcon("service.png"),
            SwingConstants.LEFT));
    node.removeAllChildren();

    serviceExplorer.setRootVisible(true);

    DefaultTreeModel model = (DefaultTreeModel) serviceExplorer.getModel();

    if (messageBus instanceof ServerMessageBus) {
      // this is the serverside bus.
      ServerMessageBus smb = (ServerMessageBus) messageBus;
      Collection<MessageCallback> receivers = smb.getReceivers(currentlySelectedService);

      DefaultMutableTreeNode receiversNode = new DefaultMutableTreeNode("Receivers (" + receivers.size() + ")", true);

      for (MessageCallback mc : receivers) {
        receiversNode.add(new DefaultMutableTreeNode(mc.getClass().getName()));

        if (mc instanceof RemoteServiceCallback) {
          RemoteServiceCallback remCB = (RemoteServiceCallback) mc;

          Set<String> endpoints = remCB.getEndpoints();

          DefaultMutableTreeNode remoteCPs = new DefaultMutableTreeNode("Callpoints (" + endpoints.size() + ")");

          for (String endpoint : endpoints) {
            String[] epParts = endpoint.split(":");

            StringAppender appender = new StringAppender(epParts[0]).append('(');
            for (int i = 1; i < epParts.length; i++) {
              appender.append(epParts[i]);
              if ((i + 1) < epParts.length)
                appender.append(", ");
            }

            remoteCPs.add(UiHelper.createIconEntry("database_connect.png", appender.append(')').toString()));
          }

          node.add(remoteCPs);
        }
      }

      node.add(receiversNode);
    }

    model.reload();

    for (int i = 0; i < serviceExplorer.getRowCount(); i++) {
      serviceExplorer.expandRow(i);
    }
  }

  public MainMonitorGUI getMainMonitorGUI() {
    return mainMonitorGUI;
  }

  public class ServicesListCellRender extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      String v = valueOf(value);
      if (v.endsWith(":RPC")) {
        setIcon(getSwIcon("database_connect.png"));
      }
      else {
        setIcon(BusTools.isReservedName(v) ? getSwIcon("database_key.png") : getSwIcon("database.png"));
      }
      setToolTipText(v);
      return this;
    }
  }
}
