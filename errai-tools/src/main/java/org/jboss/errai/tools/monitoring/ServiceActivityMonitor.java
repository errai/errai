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

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.util.BusToolsCli;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.lang.System.currentTimeMillis;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * This represents the actual individual service monitoring windows.
 */
public class ServiceActivityMonitor extends JFrame implements Attachable {
  private JTable activityTable;
  private JTable detailsTable;
  private ActivityMonitorTableModel tableModel;
  private MessageDetailsTableModel detailsModel;

  protected String busId;
  protected String service;
  protected ServerMonitorPanel serverMonitor;

  private boolean lockable;
  private boolean scrollLock = true;
  private int lastScrollAmount;

  protected ActivityProcessor.Handle handle;

  private ObjectExplorer explorer;

  protected WindowListener defaultWindowListener;

  public ServiceActivityMonitor(final ServerMonitorPanel serverMonitor, final String busId, final String service) {
    this.serverMonitor = serverMonitor;
    this.busId = busId;
    this.service = service;

    updateTitle(null);

    tableModel = new ActivityMonitorTableModel();

    activityTable = new JTable(tableModel);


    activityTable.setDefaultRenderer(Message.class, new MessageCellRenderer());
    activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    detailsModel = new MessageDetailsTableModel();

    detailsTable = new JTable(detailsModel);
    activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    activityTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        detailsModel.clear();

        Message m = UiHelper.uglyReEncode((String) tableModel.getValueAt(activityTable.getSelectedRow(), 1));
        if (m == null) return;

        for (Map.Entry<String, Object> entry : m.getParts().entrySet()) {
          detailsModel.addPart(entry.getKey(), entry.getValue());
        }

        detailsModel.fireTableRowsUpdated(0, m.getParts().size() - 1);
        detailsModel.fireTableDataChanged();
      }
    });

    detailsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        invokeLater(new Runnable() {
          public void run() {
            if (detailsTable.getSelectedRow() == -1 &&
                detailsTable.getSelectedRow() >= detailsModel.getRowCount()) return;

            Object v = detailsModel.getValueAt(detailsTable.getSelectedRow(), 1);

            explorer.setRoot(v);
            explorer.buildTree();
          }
        });
      }
    });

    final JScrollPane activityScroll;
    final JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        new JScrollPane(detailsTable), new JScrollPane(explorer = new ObjectExplorer()));
    bottomSplit.setDividerLocation(300);


    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        activityScroll = new JScrollPane(activityTable),
        bottomSplit);


    activityTable.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {
        if (!Character.isWhitespace(e.getKeyChar()) || e.getKeyChar() == '\n') {
          searchDialog.setAlwaysOnTop(true);
          searchDialog.setLocationRelativeTo(ServiceActivityMonitor.this);
          if (e.getKeyChar() != '\n') searchDialog.keyTyped(e);
          searchDialog.setVisible(true);
        }
      }

      public void keyPressed(KeyEvent e) {
      }

      public void keyReleased(KeyEvent e) {
      }
    });

    KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

    getLayeredPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(escKeyStroke, "esc-pressed");

    getLayeredPane().getActionMap().put("esc-pressed", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (tableModel.isFiltered()) {
          invokeLater(new Runnable() {
            public void run() {
              activityTable.clearSelection();
              tableModel.setFilterTerm(null);
              activityTable.setModel(tableModel);
              tableModel.fireTableDataChanged();
              updateTitle(null);
            }
          });

        }
      }
    });


    splitPane.setDividerLocation(150);

    final JScrollBar vertScroll = activityScroll.getVerticalScrollBar();
    vertScroll.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
        lockable = true;
      }

      public void mouseReleased(MouseEvent e) {
        lockable = false;
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });

    vertScroll.addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        lastScrollAmount = e.getScrollAmount();
      }
    });

    vertScroll.addAdjustmentListener(new AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        if (lockable || lastScrollAmount != 0) {
          scrollLock = (e.getValue() == e.getAdjustable().getMaximum() - e.getAdjustable().getVisibleAmount());
          return;
        }

        lastScrollAmount = 0;

        if (scrollLock) e.getAdjustable().setValue(e.getAdjustable().getMaximum());
      }
    });

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

    addWindowListener(defaultWindowListener = new WindowListener() {
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

  public void updateTitle(String s) {
    if (s == null) setTitle(service + "@" + busId);
    else setTitle(service + "@" + busId + ": " + s);
  }

  public class ActivityLogEntry {
    private long time;
    private String message;

    public ActivityLogEntry(long time, String message) {
      this.time = time;
      this.message = message;
    }

    public long getTime() {
      return time;
    }

    public void setTime(long time) {
      this.time = time;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

  public class ActivityMonitorTableModel extends AbstractTableModel {
    private ArrayList<ActivityLogEntry> messages = new ArrayList<ActivityLogEntry>();
    private ArrayList<ActivityLogEntry> filteredMessages = new ArrayList<ActivityLogEntry>();

    private volatile Pattern filter = null;

    private final String[] COLS
        = {"Time", "Message Contents"};

    private final Class[] TYPES
        = {String.class, String.class};

    private DateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS");

    @Override
    public String getColumnName(int column) {
      return COLS[column];
    }

    public int getRowCount() {
      return filter == null ? messages.size() : filteredMessages.size();
    }

    public int getColumnCount() {
      return COLS.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex == -1) return null;
      if (filter == null) {
        switch (columnIndex) {
          case 0:
            return formatter.format(new Date(messages.get(rowIndex).getTime()));
          case 1:
            return messages.get(rowIndex).getMessage();
        }
      }
      else {
        switch (columnIndex) {
          case 0:
            return formatter.format(new Date(filteredMessages.get(rowIndex).getTime()));
          case 1:
            return filteredMessages.get(rowIndex).getMessage();
        }
      }

      return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return TYPES[columnIndex];
    }

    public void addMessage(long time, String message) {
      ActivityLogEntry entry = new ActivityLogEntry(time, message);
      messages.add(entry);
      if (filter != null) {
        if (filter.matcher(message).find()) {
          filteredMessages.add(entry);
          fireTableRowsInserted(filteredMessages.size() - 1, filteredMessages.size() - 1);
        }

      }
      else {
        fireTableRowsInserted(messages.size() - 1, messages.size() - 1);
      }
    }

    public void setFilterTerm(String filterTerm) {
      if (filterTerm != null && filterTerm.length() != 0) {
        Pattern filter = Pattern.compile(filterTerm);
        this.filteredMessages.clear();
        for (ActivityLogEntry entry : messages) {
          if (filter.matcher(entry.getMessage()).find()) {
            filteredMessages.add(entry);
          }
        }
        this.filter = filter;

      }
      else {
        this.filter = null;
        this.filteredMessages.clear();
      }

    }

    public boolean isFiltered() {
      return this.filter != null;
    }
  }

  public class MessageDetailsTableModel extends AbstractTableModel {
    private ArrayList<String> fields = new ArrayList<String>();
    private ArrayList<Object> values = new ArrayList<Object>();

    private final String[] COLS = {"Message Part", "Value"};
    private final Class[] TYPES = {String.class, Object.class};

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
      if (rowIndex != -1) {
        switch (columnIndex) {
          case 0:
            if (fields.size() > rowIndex)
              return fields.get(rowIndex);
          case 1:
            if (values.size() > rowIndex)
              return values.get(rowIndex);
        }
      }
      return null;
    }

    public void addPart(String field, Object value) {
      fields.add(field);
      values.add(value);
    }

    public void clear() {
      fields.clear();
      values.clear();
    }
  }

  class SearchDialog extends JFrame {
    JTextField searchField;
    KeyEvent preEvent;

    public SearchDialog() {
      setUndecorated(true);

      setSize(300, 30);
      searchField = new JTextField();

      getContentPane().add(searchField);


      KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

      getLayeredPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(escKeyStroke, "esc-pressed");

      getLayeredPane().getActionMap().put("esc-pressed", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          searchField.setText("");
          preEvent = null;
          setVisible(false);
        }
      });

      searchField.addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent e) {
          if (e.getKeyChar() == '\n') {
            final String val = searchField.getText();
            searchField.setText("");

            invokeLater(new Runnable() {
              public void run() {
                setVisible(false);
                activityTable.clearSelection();
                try {
                  tableModel.setFilterTerm(val);
                }
                catch (PatternSyntaxException e) {
                  JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Dialog",
                      JOptionPane.ERROR_MESSAGE);
                  return;
                }

                activityTable.setModel(tableModel);
                tableModel.fireTableDataChanged();
                updateTitle("Filtering[\"" + val + "\"]");
              }
            });

          }
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }
      });

      searchField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          if (preEvent != null) {
            searchField.setText(new String(new char[]{preEvent.getKeyChar()}));
            searchField.setCaretPosition(1);
            preEvent = null;
          }
        }

        public void focusLost(FocusEvent e) {
          searchField.setText("");
          preEvent = null;
          setVisible(false);
        }
      });

    }

    public void keyTyped(KeyEvent s) {
      preEvent = s;
    }


  }

  private SearchDialog searchDialog = new SearchDialog();


  public void notifyMessage(long time, Message message) {
    /*
    * This is a huge hack to get the display of the messages consistent with what the payload does when
    * encoded. And no it's not particularly efficient.  But since inbus messages are not encoded by JSON and
    * it would be wacky to make the monitoring API such that we had to scan for one or the other, this
    * is much more consistent from an API point-of-view.
    */
    tableModel.addMessage(time, BusToolsCli.encodeMessage(message));
  }

  public void attach(ActivityProcessor proc) {
    handle = proc.registerEvent(EventType.MESSAGE, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        Message m = (Message) event.getContents();
        // if the message is sent to the currently monitored bus (or is global) and the subject matches, then notify
        if ((event.getToBus().equals(busId) || !m.isFlagSet(RoutingFlag.NonGlobalRouting)) && service.equals(event.getSubject())) {
          notifyMessage(event.getTime(), (Message) event.getContents());
        }
      }
    });

    /**
     * When this monitor is attached send a request to replay the messages for this subject@bus.
     */
    proc.notifyEvent(currentTimeMillis(), EventType.REPLAY_MESSAGES, SubEventType.NONE, busId, busId, service, null, null, false);
  }
}
