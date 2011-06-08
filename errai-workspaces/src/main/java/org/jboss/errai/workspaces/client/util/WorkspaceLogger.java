/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

/* jboss.org */
package org.jboss.errai.workspaces.client.util;

import com.allen_sauer.gwt.log.client.Log;
import com.allen_sauer.gwt.log.client.LogRecord;
import com.allen_sauer.gwt.log.client.LogUtil;
import com.allen_sauer.gwt.log.client.Logger;
import com.allen_sauer.gwt.log.client.impl.LogClientBundle;
import com.allen_sauer.gwt.log.client.util.DOMUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.ToolBar;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.BorderLayoutData;

/**
 * A slightly modified version of the orig div logger.
 *
 * @author Fred Saur
 * @author Heiko Braun
 */
public class WorkspaceLogger implements Logger {
  // CHECKSTYLE_JAVADOC_OFF

  private static final int[] levels = {
      Log.LOG_LEVEL_TRACE, Log.LOG_LEVEL_DEBUG, Log.LOG_LEVEL_INFO, Log.LOG_LEVEL_WARN,
      Log.LOG_LEVEL_ERROR, Log.LOG_LEVEL_FATAL, Log.LOG_LEVEL_OFF,};
  private static final int MAX_VERTICAL_SCROLL = 0x6666666;

  private static final String STACKTRACE_ELEMENT_PREFIX = "&nbsp;&nbsp;&nbsp;&nbsp;at&nbsp;";
  private static final int UPDATE_INTERVAL_MILLIS = 500;
  private boolean dirty = false;
  private Button[] levelButtons;

  private final org.gwt.mosaic.ui.client.layout.LayoutPanel logDockPanel =
      new org.gwt.mosaic.ui.client.layout.LayoutPanel(new BorderLayout());
  private String logText = "";

  private final HTML logTextArea = new HTML();

  private final ScrollLayoutPanel scrollPanel = new ScrollLayoutPanel();
  private final Timer timer;

  public interface ThresholdNotification {
    void onLogLevel(int level);
  }

  private ThresholdNotification notification;

  /**
   * Default constructor.
   */
  public WorkspaceLogger(ThresholdNotification notification) {

    this.notification = notification;

    logDockPanel.addStyleName(LogClientBundle.INSTANCE.css().logPanel());
    logTextArea.addStyleName(LogClientBundle.INSTANCE.css().logTextArea());
    scrollPanel.addStyleName(LogClientBundle.INSTANCE.css().logScrollPanel());

    // scrollPanel.setAlwaysShowScrollBars(true);

    final Widget headerPanel = makeHeader();

    logDockPanel.add(headerPanel, new BorderLayoutData(BorderLayout.Region.NORTH, "30 px", false));
    logDockPanel.add(scrollPanel, new BorderLayoutData(BorderLayout.Region.CENTER));

    scrollPanel.add(logTextArea);

    //logDockPanel.setVisible_(false);    

    timer = new Timer() {
      @Override
      public void run() {
        dirty = false;
        logTextArea.setHTML(logTextArea.getHTML() + logText);
        logText = "";
        DeferredCommand.addCommand(
            new Command() {
              public void execute() {
                scrollPanel.setScrollPosition(MAX_VERTICAL_SCROLL);
              }
            });
      }
    };
  }

  public final void clear() {
    logTextArea.setHTML("");
  }

  public final Widget getWidget() {
    return logDockPanel;
  }

  public final boolean isSupported() {
    return true;
  }

  public final boolean isVisible() {
    return logDockPanel.isAttached() && logDockPanel.isVisible();
  }

  public void log(LogRecord record) {

    String text = record.getFormattedMessage().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    String title = makeTitle(record);
    Throwable throwable = record.getThrowable();
    if (throwable != null) {
      while (throwable != null) {
        text += throwable.getClass().getName() + ":<br><b>" + throwable.getMessage() + "</b>";
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        if (stackTraceElements.length > 0) {
          text += "<div class='log-stacktrace'>";
          for (StackTraceElement element : stackTraceElements) {
            text += STACKTRACE_ELEMENT_PREFIX + element + "<br>";
          }
          text += "</div>";
        }
        throwable = throwable.getCause();
        if (throwable != null) {
          text += "Caused by: ";
        }
      }
    }
    text = text.replaceAll("\r\n|\r|\n", "<BR>");
    addLogText("<div class='" + LogClientBundle.INSTANCE.css().logMessage()
        + "' onmouseover='className+=\" log-message-hover\"' "
        + "onmouseout='className=className.replace(/ log-message-hover/g,\"\")' style='color: "
        + getColor(record.getLevel()) + "' title='" + title + "'>" + text + "</div>");

    // notify enclosing component about log level threshold
    notification.onLogLevel(record.getLevel());
  }

  /*public final void moveTo(int x, int y) {
    RootPanel.get().add(logDockPanel, x, y);
  }*/

  public void setCurrentLogLevel(int level) {
    for (int i = 0; i < levels.length; i++) {
      if (levels[i] < Log.getLowestLogLevel()) {
        levelButtons[i].setEnabled(false);
      }
      else {
        String levelText = LogUtil.levelToString(levels[i]);
        boolean current = level == levels[i];
        levelButtons[i].setTitle(current ? "Current (runtime) log level is already '" + levelText
            + "'" : "Set current (runtime) log level to '" + levelText + "'");
        boolean active = level <= levels[i];
        DOM.setStyleAttribute(levelButtons[i].getElement(), "color", active ? getColor(levels[i])
            : "#ccc");
      }
    }
  }

  public final void setPixelSize(int width, int height) {
    logTextArea.setPixelSize(width, height);
  }

  public final void setSize(String width, String height) {
    logTextArea.setSize(width, height);
  }

  private void addLogText(String logTest) {
    logText += logTest;
    if (!dirty) {
      dirty = true;
      timer.schedule(UPDATE_INTERVAL_MILLIS);
    }
  }

  private String getColor(int logLevel) {
    if (logLevel == Log.LOG_LEVEL_OFF) {
      return "#000"; // black
    }
    if (logLevel >= Log.LOG_LEVEL_FATAL) {
      return "#CC0000"; // bright red
    }
    if (logLevel >= Log.LOG_LEVEL_ERROR) {
      return "#990000"; // dark red
    }
    if (logLevel >= Log.LOG_LEVEL_WARN) {
      return "#CC9900"; // dark orange
    }
    if (logLevel >= Log.LOG_LEVEL_INFO) {
      return "#336699"; // blue
    }
    if (logLevel >= Log.LOG_LEVEL_DEBUG) {
      return "#336633"; // green
    }
    return "#F0F"; // purple
  }

  /**
   * @deprecated
   */
  @Deprecated
  private Widget makeHeader() {

    ToolBar masterPanel = new ToolBar();
    //masterPanel.setWidth("100%");


    final Label titleLabel = new Label("System Messages", false);
    titleLabel.setStylePrimaryName(LogClientBundle.INSTANCE.css().logTitle());

    HorizontalPanel buttonPanel = new HorizontalPanel();
    levelButtons = new Button[levels.length];
    for (int i = 0; i < levels.length; i++) {
      final int level = levels[i];
      levelButtons[i] = new Button(LogUtil.levelToString(level));
      buttonPanel.add(levelButtons[i]);
      levelButtons[i].addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          ((Button) event.getSource()).setFocus(false);
          Log.setCurrentLogLevel(level);
        }
      });
    }

    Button clearButton = new Button("Clear");
    clearButton.addStyleName(LogClientBundle.INSTANCE.css().logClearButton());
    DOM.setStyleAttribute(clearButton.getElement(), "color", "#00c");
    clearButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        ((Button) event.getSource()).setFocus(false);
        Log.clear();
      }
    });
    buttonPanel.add(clearButton);

    //masterPanel.add(titleLabel);
    masterPanel.add(buttonPanel);

    return masterPanel;
  }

  private String makeTitle(LogRecord record) {
    String message = record.getFormattedMessage();
    Throwable throwable = record.getThrowable();
    if (throwable != null) {
      if (throwable.getMessage() == null) {
        message = throwable.getClass().getName();
      }
      else {
        message = throwable.getMessage().replaceAll(
            throwable.getClass().getName().replaceAll("^(.+\\.).+$", "$1"), "");
      }
    }
    return DOMUtil.adjustTitleLineBreaks(message).replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll(
        "'", "\"");
  }

}
