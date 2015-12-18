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

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ServerLogPanel extends JFrame implements Attachable {
  private ServerLogModel serverLogModel;

  private ActivityProcessor.Handle handle;

  public ServerLogPanel(MainMonitorGUI mainMonitorGUI) {
    serverLogModel = new ServerLogModel();

    JTable activityTable = new JTable(serverLogModel);
    activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    activityTable.setDefaultRenderer(LogMessage.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Object msg = ((LogMessage) value).getMessage();

        if (msg != null) {
          String txt = String.valueOf(msg);
          setText(txt);
          setToolTipText(txt);
        }
        else {
          setText("--");
        }

        return this;
      }
    });

    getContentPane().add(new JScrollPane(activityTable));

    Point point = mainMonitorGUI.getLocation();
    setLocation(point.x + 20, point.y + 20);
    setSize(500, 300);

    addWindowListener(new WindowListener() {
      public void windowOpened(WindowEvent e) {
      }

      public void windowClosing(WindowEvent e) {
      }

      public void windowClosed(WindowEvent e) {
        handle.dispose();
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

    setTitle("Monitoring Bus");
    setVisible(true);
  }

  public class ServerLogEntry {
    private long time;
    private EventType eventType;
    private SubEventType subEventType;
    private String subject;
    private LogMessage message;

    public ServerLogEntry(long time, EventType eventType, SubEventType subEventType, String subject, LogMessage message) {
      this.time = time;
      this.eventType = eventType;
      this.subEventType = subEventType;
      this.subject = subject;
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
      return (message==null) ? "" : String.valueOf(message);
    }
  }

  public class ServerLogModel extends AbstractTableModel {
    private ArrayList<ServerLogEntry> entries = new ArrayList<ServerLogEntry>();

    private final String[] COLS
        = {"Time", "Event Type", "Sub-Event Type", "Details", "Message"};

    private final Class[] TYPES
        = {String.class, Enum.class, Enum.class, String.class, LogMessage.class};

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
          return entries.get(rowIndex).subject;
        case 4:
          return entries.get(rowIndex).message;
      }

      return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return TYPES[columnIndex];
    }

    public void addMessage(long time, EventType eventType, SubEventType subEventType, String subject, Object message) {
      entries.add(new ServerLogEntry(time, eventType, subEventType, subject, new LogMessage(message)));
      fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
    }
  }

  public class MessageDetailsModel extends AbstractTableModel {
    public int getRowCount() {
      return 0;
    }

    public int getColumnCount() {
      return 0;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      return null;
    }
  }

  public void attach(ActivityProcessor proc) {
    handle = proc.registerEvent(EventType.BUS_EVENT, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        String details = null;
        switch (event.getSubType()) {
          case REMOTE_UNSUBSCRIBE:
          case REMOTE_SUBSCRIBE:
          case SERVER_SUBSCRIBE:
          case SERVER_UNSUBSCRIBE:
            details = event.getSubject();
            break;
          case REMOTE_ATTACHED:
            details = event.getFromBus();
            break;
        }

        addMessage(event.getTime(), EventType.BUS_EVENT, event.getSubType(), details, event.getContents());
      }
    });

    proc.notifyEvent(System.currentTimeMillis(), EventType.REPLAY_BUS_EVENTS, SubEventType.NONE, null, null, null, null, null, false);
  }

  public void addMessage(long time, EventType eventType, SubEventType subEventType, String subject, Object message) {
    serverLogModel.addMessage(time, eventType, subEventType, subject, message);
  }
}

