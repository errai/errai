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
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ServerLogPanel extends JFrame {
    private JScrollPane tableScroll;
    private ServerLogModel serverLogModel;
    private MainMonitorGUI mainMonitorGUI;

    public ServerLogPanel(MainMonitorGUI mainMonitorGUI) {
        this.mainMonitorGUI = mainMonitorGUI;

        serverLogModel = new ServerLogModel();
        JTable activityTable = new JTable(serverLogModel);
        activityTable.setModel(serverLogModel);
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        Point point = mainMonitorGUI.getLocation();
        setLocation(point.x + 20, point.y + 20);
        setSize(500, 300);
    }

    public class ServerLogEntry {
        private long time;
        private EventType eventType;
        private SubEventType subEventType;
        private LogMessage message;

        public ServerLogEntry(long time, EventType eventType, SubEventType subEventType, LogMessage message) {
            this.time = time;
            this.eventType = eventType;
            this.subEventType = subEventType;
            this.message = message;
        }
    }

    /**
     * A simple wrapper for Swing's formatting purposes.
     */
    public class LogMessage {
        private Object message;

        public LogMessage(Object message) {
            this.message = message;
        }

        public Object getMessage() {
            return message;
        }
    }

    public class ServerLogModel extends AbstractTableModel {
        private ArrayList<ServerLogEntry> entries = new ArrayList<ServerLogEntry>();

        private final String[] COLS
                = {"Time", "Event Type", "Sub-Event Type", "Message"};

        private final Class[] TYPES
                = {String.class, Enum.class, Enum.class, LogMessage.class};

        private DateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS");

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        public int getRowCount() {
            return entries.size();
        }

        public int getColumnCount() {
            return COLS.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return formatter.format(new Date(entries.get(rowIndex).time));
                case 1:
                    return entries.get(rowIndex).eventType;
                case 2:
                    return entries.get(rowIndex).subEventType;
                case 3:
                    return entries.get(rowIndex).message;
            }

            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return TYPES[columnIndex];
        }

        public void addMessage(long time, EventType eventType, SubEventType subEventType, Object message) {
            entries.add(new ServerLogEntry(time, eventType, subEventType, new LogMessage(message)));
            fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
        }
    }

    private void loadData() {

    }

    public void addMessage(long time, EventType eventType, SubEventType subEventType, Object message) {
        serverLogModel.addMessage(time, eventType, subEventType, message);
    }
}
