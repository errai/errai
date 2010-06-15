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

import static java.lang.System.currentTimeMillis;
import static javax.swing.SwingUtilities.invokeLater;
import static org.jboss.errai.tools.monitoring.UiHelper.uglyReEncode;

public class ServiceActivityMonitor extends JFrame implements Attachable {
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

        setTitle(service + "@" + busId);

        tableModel = new ActivityMonitorTableModel();

        final JTable activityTable = new JTable(tableModel);
        activityTable.setDefaultRenderer(Message.class, new MessageCellRenderer());
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        detailsModel = new MessageDetailsTableModel();

        final JTable detailsTable = new JTable(detailsModel);
        activityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        activityTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                detailsModel.clear();

                Message m = (Message) tableModel.getValueAt(activityTable.getSelectedRow(), 1);

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
                        explorer.setRoot(detailsModel.getValueAt(detailsTable.getSelectedRow(), 1));
                        explorer.buildTree();
                    }
                });
            }
        });

        final JScrollPane activityScroll;
        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(detailsTable), new JScrollPane(explorer = new ObjectExplorer()));
        bottomSplit.setDividerLocation(300);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                activityScroll = new JScrollPane(activityTable), bottomSplit);
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

        private boolean scrollLock;

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

        public void addMessage(long time, Message message) {
            messages.add(new AcvityLogEntry(time, message));
            fireTableRowsInserted(messages.size() - 1, messages.size() - 1);
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
            switch (columnIndex) {
                case 0:
                    if (fields.size() > rowIndex)
                        return fields.get(rowIndex);
                case 1:
                    if (values.size() > rowIndex)
                        return values.get(rowIndex);
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


    public void notifyMessage(long time, Message message) {
        /*
         * This is a huge hack to get the display of the messages consistent with what the payload does when
         * encoded. And no it's not particularly efficient.  But since inbus messages are not encoded by JSON and
         * it would be wacky to make the monitoring API such that we had to scan for one or the other, this
         * is much more consistent from an API point-of-view.
         */
        tableModel.addMessage(time, uglyReEncode(message));
    }

    public void attach(ActivityProcessor proc) {
        handle = proc.registerEvent(EventType.MESSAGE, new MessageMonitor() {
            public void monitorEvent(MessageEvent event) {
                if (service.equals(event.getSubject()))
                    notifyMessage(event.getTime(), (Message) event.getContents());
            }
        });

        /**
         * When this monitor is attached send a request to replay the messages for this subject@bus.
         */
        proc.notifyEvent(currentTimeMillis(), EventType.REPLAY_MESSAGES, SubEventType.NONE, busId, busId, service, null, null, false);
    }
}
