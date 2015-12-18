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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.regex.Pattern;

public class ConversationActivityMonitor extends ServiceActivityMonitor {

  private final Pattern MATCHER;

  public ConversationActivityMonitor(final ServerMonitorPanel serverMonitor, final String busId, final String service) {
    super(serverMonitor, busId, service);
    updateTitle(null);

    removeWindowListener(defaultWindowListener);

    addWindowListener(new WindowListener() {
      public void windowOpened(WindowEvent e) {
      }

      public void windowClosing(WindowEvent e) {
      }

      public void windowClosed(WindowEvent e) {
        handle.dispose();
        serverMonitor.stopMonitor(service + ":Conversations");
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

    MATCHER = Pattern.compile(service + ".*:RespondTo:.*");
  }

  @Override
  public void attach(ActivityProcessor proc) {
    handle = proc.registerEvent(EventType.MESSAGE, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        String incomingSubject = event.getSubject();
        if (MATCHER.matcher(incomingSubject).matches()) {
          notifyMessage(event.getTime(), (Message) event.getContents());
        }
      }
    });

    proc.notifyEvent(System.currentTimeMillis(), EventType.REPLAY_MESSAGES, SubEventType.NONE, null, null, service + "%:RespondTo:%", null, null, false);
  }

  public void updateTitle(String s) {
    if (s == null) setTitle("Conversations: " + service + "@" + busId);
    else setTitle("Conversations: " + service + "@" + busId + ": " + s);
  }
}
