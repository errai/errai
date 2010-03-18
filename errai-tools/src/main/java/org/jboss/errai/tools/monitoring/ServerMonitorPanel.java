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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ServerMonitorPanel {
    private MainMonitorGUI mainMonitorGUI;

    private JLabel serviceName;
    private JButton monitorButton;
    private JScrollPane serviceListScroll;
    private JList busServices;
    private final DefaultListModel busServicesModel;
    private JTree tree1;
    private JButton activityConsoleButton;
    private JPanel rootPanel;

    private Map<String, ServiceActityMonitor> monitors = new HashMap<String, ServiceActityMonitor>();

    public ServerMonitorPanel(MainMonitorGUI gui) {
        this.mainMonitorGUI = gui;

        serviceListScroll.setDoubleBuffered(true);
         busServices.setDoubleBuffered(true);


         busServices.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         busServices.setModel(busServicesModel = new DefaultListModel());

         busServices.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 serviceName.setText(getCurrentServiceSelection());
             }
         });


         monitorButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (monitors.containsKey(getCurrentServiceSelection())) {
                     monitors.get(getCurrentServiceSelection()).toFront();
                 } else {
                     monitors.put(getCurrentServiceSelection(), new ServiceActityMonitor(mainMonitorGUI));
                 }
             }
         });
    }

    private String getCurrentServiceSelection() {
        return String.valueOf(busServicesModel.get(busServices.getSelectedIndex()));
    }

    public void addRemoteQueue(final Object sessionRef) {

    }

    public void addServiceName(final String serviceName) {
        synchronized (busServicesModel) {
            if (busServicesModel.contains(serviceName)) return;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    busServicesModel.addElement(serviceName);
                }
            });

        }
    }

    public void removeServiceName(final String serviceName) {
        synchronized (busServicesModel) {
            if (!busServicesModel.contains(serviceName)) return;

            SwingUtilities.invokeLater(new Runnable() {
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
}
