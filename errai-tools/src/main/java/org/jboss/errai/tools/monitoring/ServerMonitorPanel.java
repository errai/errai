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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.swing.SwingUtilities.invokeLater;

public class ServerMonitorPanel implements Attachable {
    private MainMonitorGUI mainMonitorGUI;
    private MessageBus messageBus;
    private String busId;

    private JButton monitorButton;
    private JList busServices;
    private final DefaultListModel busServicesModel;

    private JButton activityConsoleButton;
    private JPanel rootPanel;
    private JScrollPane serviceScrollPane;
    private JTree serviceExplorer;
    private JPanel serviceListArea;

    private String currentlySelectedService;

    private ServerLogPanel logPanel;

    private ActivityProcessor processor;

    private Map<String, ServiceActivityMonitor> monitors = new HashMap<String, ServiceActivityMonitor>();

    public ServerMonitorPanel(MainMonitorGUI gui, MessageBus bus, String busId) {
        this.mainMonitorGUI = gui;
        this.messageBus = bus;
        this.busId = busId;

        busServices = new JList();
        busServices.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        busServices.setModel(busServicesModel = new DefaultListModel());
        busServices.setCellRenderer(new ServicesListCellRender());

        serviceListArea.add(new JScrollPane(busServices));
        

        busServices.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                currentlySelectedService = getCurrentServiceSelection();
                generateServiceExplorer();
            }
        });

        busServices.addMouseListener(new MouseListener() {
            long lastClick;

            public void mouseClicked(MouseEvent e) {
                switch (e.getClickCount()) {
                    case 1:
                        lastClick = System.currentTimeMillis();
                        break;
                    case 2:
                        if (!e.isConsumed() && (System.currentTimeMillis() - lastClick < 500)) {
                            e.consume();
                            openActivityMonitor();
                        }
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        monitorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openActivityMonitor();
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
        } else {
            ServiceActivityMonitor sam = new ServiceActivityMonitor(this, busId, currentlySelectedService);
            sam.attach(processor);
            monitors.put(currentlySelectedService, sam);
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

    public ServiceActivityMonitor getMonitor(String monitor) {
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
                            //     DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(String.valueOf(o));

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

    public MainMonitorGUI getMainMonitorGUI() {
        return mainMonitorGUI;
    }

    private Icon getIcon(String name) {
        return new ImageIcon(this.getClass().getClassLoader().getResource(name));
    }

    private MutableTreeNode createIconEntry(String icon, String name) {
        return new DefaultMutableTreeNode(new JLabel(name, getIcon(icon), SwingConstants.LEFT));
    }

    public class ServicesListCellRender extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setToolTipText(String.valueOf(value));
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
