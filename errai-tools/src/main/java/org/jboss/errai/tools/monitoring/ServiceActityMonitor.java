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

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.CommandMessage;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.util.ArrayList;
import java.util.HashMap;

public class ServiceActityMonitor extends JFrame {

    private ActivityMonitorTableModel tableModel;

    private JPanel rootPanel;
    private JPanel tableArea;
    private JButton pauseButton;
    private JScrollPane tableScroll;
    private JPanel tableHeader;
    private MainMonitorGUI monitorGUI;

    public static void main(String[] args) {
        ServiceActityMonitor sam = new ServiceActityMonitor(null);
        sam.notifyMessage(CommandMessage.createWithParts(new HashMap()));
    }

    public ServiceActityMonitor(MainMonitorGUI monitorGUI) {
        this.monitorGUI = monitorGUI;

        tableModel = new ActivityMonitorTableModel();

        JTable activityTable = new JTable(tableModel);
        activityTable.setModel(tableModel);

        tableArea.add(activityTable);
        tableHeader.add(activityTable.getTableHeader());

        setSize(500,300);

        getContentPane().add(rootPanel);

        setVisible(true);
    }

    public class AcvityLogEntry {
        private long time;
        private Message message;

        public AcvityLogEntry(long time, Message message) {
            this.time = time;
            this.message = message;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public class ActivityMonitorTableModel extends AbstractTableModel {
        private ArrayList<AcvityLogEntry> messages = new ArrayList<AcvityLogEntry>();

        private final String[] COLS
                = {"Time", "Message Contents"};

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        public int getRowCount() {
            return messages.size();
        }

        public int getColumnCount() {
            return COLS.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return messages.get(rowIndex).getTime();
                case 1:
                    return messages.get(rowIndex).getMessage().toString();
            }

            return null;
        }

        public void addMessage(Message message) {
            messages.add(new AcvityLogEntry(System.currentTimeMillis(), message));
            fireTableRowsInserted(messages.size() - 1, messages.size() -1);
        }
    }

    public void notifyMessage(Message message) {
        tableModel.addMessage(message);
    }
}
