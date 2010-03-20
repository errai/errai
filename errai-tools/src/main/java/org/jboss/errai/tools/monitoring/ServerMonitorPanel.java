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

import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.RuleDelegateMessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.security.auth.rules.RolesRequiredRule;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.swing.SwingUtilities.invokeLater;

public class ServerMonitorPanel {
    private MainMonitorGUI mainMonitorGUI;
    private MessageBus messageBus;
    private JButton monitorButton;
    private JScrollPane serviceListScroll;
    private JList busServices;
    private final DefaultListModel busServicesModel;

    private JButton activityConsoleButton;
    private JPanel rootPanel;
    private JScrollPane serviceScrollPane;
    private JTree serviceExplorer;

    private String currentlySelectedService;

    private Map<String, ServiceActityMonitor> monitors = new HashMap<String, ServiceActityMonitor>();

    public ServerMonitorPanel(MainMonitorGUI gui, MessageBus bus) {
        this.mainMonitorGUI = gui;
        this.messageBus = bus;

        serviceListScroll.setDoubleBuffered(true);
        busServices.setDoubleBuffered(true);
        busServices.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        busServices.setModel(busServicesModel = new DefaultListModel());

        busServices.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                currentlySelectedService = getCurrentServiceSelection();
                generateServiceExplorer();
            }
        });

        monitorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (monitors.containsKey(getCurrentServiceSelection())) {
                    monitors.get(currentlySelectedService).toFront();
                } else {
                    monitors.put(currentlySelectedService, new ServiceActityMonitor(mainMonitorGUI));
                }
            }
        });

        DefaultTreeModel model = (DefaultTreeModel) serviceExplorer.getModel();
        ((DefaultMutableTreeNode) serviceExplorer.getModel().getRoot()).removeAllChildren();
        serviceExplorer.setRootVisible(false);

        serviceExplorer.setCellRenderer(new MonitorTreeCellRenderer());

        serviceExplorer.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        model.reload();
    }

    private String getCurrentServiceSelection() {
        return String.valueOf(busServicesModel.get(busServices.getSelectedIndex()));
    }

    public void addServiceName(final String serviceName) {
        synchronized (busServicesModel) {
            if (busServicesModel.contains(serviceName)) return;

            invokeLater(new Runnable() {
                public void run() {
                    busServicesModel.addElement(serviceName);
                }
            });

        }
    }

    public void removeServiceName(final String serviceName) {
        synchronized (busServicesModel) {
            if (!busServicesModel.contains(serviceName)) return;

            invokeLater(new Runnable() {
                public void run() {
                    busServicesModel.removeElement(serviceName);
                }
            });
        }
    }

    public ServiceActityMonitor getMonitor(String monitor) {
        return monitors.get(monitor);
    }

    public JPanel getPanel() {
        return rootPanel;
    }

    private void generateServiceExplorer() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) serviceExplorer.getModel().getRoot();

        node.setUserObject(new JLabel(currentlySelectedService, getIcon("service.png"), SwingConstants.LEFT));
        node.removeAllChildren();

        serviceExplorer.setRootVisible(true);

        DefaultTreeModel model = (DefaultTreeModel) serviceExplorer.getModel();


        if (messageBus instanceof ServerMessageBus) {
            // this is the serverside bus.
            ServerMessageBus smb = (ServerMessageBus) messageBus;
            List<MessageCallback> receivers = smb.getReceivers(currentlySelectedService);

            DefaultMutableTreeNode receiversNode
                    = new DefaultMutableTreeNode("Receivers (" + receivers.size() + ")", true);

            for (MessageCallback mc : receivers) {
                receiversNode.add(new DefaultMutableTreeNode(mc.getClass().getName()));

                if (mc instanceof RuleDelegateMessageCallback) {
                    RuleDelegateMessageCallback ruleDelegate = (RuleDelegateMessageCallback) mc;
                    DefaultMutableTreeNode securityNode =
                            new DefaultMutableTreeNode("Security");

                    if (ruleDelegate.getRoutingRule() instanceof RolesRequiredRule) {
                        RolesRequiredRule rule = (RolesRequiredRule) ruleDelegate.getRoutingRule();

                        DefaultMutableTreeNode rolesNode =
                                new DefaultMutableTreeNode(rule.getRoles().isEmpty() ? "Requires Authentication" : "Roles Required");

                        for (Object o : rule.getRoles()) {
                            DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(String.valueOf(o));
                          
                            rolesNode.add(createIconEntry("key.png", String.valueOf(o)));
                        }

                        securityNode.add(rolesNode);
                    }

                    node.add(securityNode);
                }
            }

            node.add(receiversNode);
        }

        model.reload();
    }

    private Icon getIcon(String name) {
        return new ImageIcon(this.getClass().getClassLoader().getResource(name));
    }

    private MutableTreeNode createIconEntry(String icon, String name) {
        return new DefaultMutableTreeNode(new JLabel(name, getIcon(icon), SwingConstants.LEFT));
    }
}
