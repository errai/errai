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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServiceActivityMonitor extends JFrame implements Attachable {
    private ActivityMonitorTableModel tableModel;
    private MessageDetailsTableModel detailsModel;

    private String busId;
    private String service;
    private ServerMonitorPanel serverMonitor;

    ActivityProcessor.Handle handle;

    public ServiceActivityMonitor(final ServerMonitorPanel serverMonitor, final String busId, final String service) {
        this.serverMonitor = serverMonitor;
        this.busId = busId;
        this.service = service;

        setTitle(service + "@" + busId);

        tableModel = new ActivityMonitorTableModel();

        final JTable activityTable = new JTable(tableModel);
        activityTable.setDefaultRenderer(Message.class, new MessageCellRenderer());
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        detailsModel = new MessageDetailsTableModel();

        JTable detailsTable = new JTable(detailsModel);
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        activityTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                detailsModel.clear();

                Message m = (Message) tableModel.getValueAt(activityTable.getSelectedRow(), 1);

                for (Map.Entry<String, Object> entry : m.getParts().entrySet()) {
                    detailsModel.addPart(entry.getKey(), String.valueOf(entry.getValue()));
                }


                detailsModel.fireTableRowsUpdated(0, m.getParts().size() - 1);
                detailsModel.fireTableDataChanged();
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(activityTable), new JScrollPane(detailsTable));
        splitPane.setDividerLocation(150);

        getContentPane().add(splitPane);

        Point point = serverMonitor.getMainMonitorGUI().getLocation();
        setLocation(point.x + 20, point.y + 20);
        setSize(500, 300);

        DefaultTableColumnModel defaultColumn = (DefaultTableColumnModel) activityTable.getColumnModel();

        defaultColumn.getColumn(0).setResizable(false);
        defaultColumn.getColumn(0).setPreferredWidth(120);
        defaultColumn.getColumn(0).setMaxWidth(120);

        defaultColumn = (DefaultTableColumnModel) detailsTable.getColumnModel();
        defaultColumn.getColumn(0).setPreferredWidth(120);
        defaultColumn.getColumn(0).setMaxWidth(250);



        setVisible(true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
            }

            public void windowClosed(WindowEvent e) {
                handle.dispose();
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
            fireTableRowsInserted(messages.size() - 1, messages.size() - 1);
        }


    }

    public class MessageDetailsTableModel extends AbstractTableModel {
        private ArrayList<String> fields = new ArrayList<String>();
        private ArrayList<String> values = new ArrayList<String>();

        private final String[] COLS = {"Message Part", "Value"};
        private final Class[] TYPES = {String.class, String.class};

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return TYPES[columnIndex];
        }

        public int getRowCount() {
            return fields.size();
        }

        public int getColumnCount() {
            return COLS.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return fields.get(rowIndex);
                case 1:
                    return values.get(rowIndex);
            }
            return null;
        }

        public void addPart(String field, String value) {
            fields.add(field);
            values.add(value);
        }

        public void clear() {
            fields.clear();
            values.clear();
        }
    }

    public void notifyMessage(Message message) {
        tableModel.addMessage(message);
    }

    public void attach(ActivityProcessor proc) {
        handle = proc.registerEvent(EventType.MESSAGE, new MessageMonitor() {
            public void monitorEvent(MessageEvent event) {
                if (service.equals(event.getSubject()))
                    notifyMessage((Message) event.getContents());
            }
        });

        proc.notifyEvent(EventType.REPLAY_MESSAGES, SubEventType.NONE, busId, busId, service, null, null, false);
    }

}
