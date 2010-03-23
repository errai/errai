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

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ServiceActityMonitor extends JFrame {

    private ActivityMonitorTableModel tableModel;

    private JPanel rootPanel;
    private JPanel tableArea;
    private JButton pauseButton;
    private JScrollPane tableScroll;
    private JPanel tableHeader;

    private String busId;
    private String service;
    private ServerMonitorPanel serverMonitor;
    
    public ServiceActityMonitor(final ServerMonitorPanel serverMonitor, final String busId, final String service) {
        this.serverMonitor = serverMonitor;
        this.busId = busId;
        this.service = service;

        setTitle(service + "@" + busId);

        tableModel = new ActivityMonitorTableModel();

        JTable activityTable = new JTable(tableModel);
        activityTable.setModel(tableModel);
        activityTable.setDefaultRenderer(Message.class, new MessageCellRenderer());
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        DefaultTableColumnModel defaultColumn = (DefaultTableColumnModel) activityTable.getColumnModel();
     
        tableArea.add(activityTable);
        tableHeader.add(activityTable.getTableHeader());

        Point point = serverMonitor.getMainMonitorGUI().getLocation();
        setLocation(point.x + 20, point.y + 20);
        setSize(500,300);

        getContentPane().add(rootPanel);
        defaultColumn.getColumn(0).setResizable(false);
        defaultColumn.getColumn(0).setPreferredWidth(120);
        defaultColumn.getColumn(0).setMaxWidth(120);

        setVisible(true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
            }

            public void windowClosed(WindowEvent e) {
                serverMonitor.stopMonitor(service);
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }
        });
        
        java.util.List<DataStore.Record> r
                = serverMonitor.getMainMonitorGUI().getDataStore().getAllMessages(busId, service);

        for (DataStore.Record rec : r) {
            notifyMessage(rec.getMessage());
        }
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

        private final Class[] TYPES
                = {String.class, Message.class};

        private DateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS");

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
                    return formatter.format(new Date(messages.get(rowIndex).getTime()));
                case 1:
                    return messages.get(rowIndex).getMessage();
            }

            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return TYPES[columnIndex];
        }

        public void addMessage(Message message) {
            messages.add(new AcvityLogEntry(System.currentTimeMillis(), message));
            fireTableRowsInserted(messages.size() - 1, messages.size() -1);
        }
    }

    public void notifyMessage(Message message) {
        tableModel.addMessage(message);
    }

    public class MessageCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setToolTipText(String.valueOf(value));
            return super.getTableCellRendererComponent(table,value,isSelected,hasFocus, row, column);
        }
    }
}
