/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.util;

import static org.jboss.errai.common.client.util.LogUtil.displayDebuggerUtilityTitle;
import static org.jboss.errai.common.client.util.LogUtil.displaySeparator;
import static org.jboss.errai.common.client.util.LogUtil.nativeLog;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.transports.TransportHandler;
import org.jboss.errai.bus.client.framework.transports.TransportStatistics;
import org.jboss.errai.common.client.util.LogUtil;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public class ManagementConsole {
  private final ClientMessageBusImpl clientMessageBus;
  private BusErrorDialog errorDialog;

  public ManagementConsole(final ClientMessageBusImpl clientMessageBus) {
    this.clientMessageBus = clientMessageBus;
    this.errorDialog = new BusErrorDialog(clientMessageBus);
    declareDebugFunction();
  }

  public void displayError(final String message, final String additionalDetails, final Throwable e) {
    showError(message + " -- Additional Details: " + additionalDetails, e);
  }

  private void showError(final String message, final Throwable e) {
    errorDialog.addError(message, "", e);

    if (LogUtil.isNativeJavaScriptLoggerSupported()) {
      nativeLog(message);
    }
  }


  private native void declareDebugFunction() /*-{
      var thisRef = this;

      $wnd.errai_status = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::displayStatus()();
      };

      $wnd.errai_list_services = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::listServices()();
      };

      $wnd.errai_show_error_console = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::showErrorConsole()();
      }
  }-*/;

  private void listServices() {

    displayDebuggerUtilityTitle("Service and Routing Table");
    nativeLog("[REMOTES]");

    for (final String remoteName : clientMessageBus.getRemoteServices()) {
      nativeLog(remoteName);
    }

    nativeLog("[LOCALS]");

    for (final String localName : clientMessageBus.getLocalServices()) {
      nativeLog(localName);
    }

    displaySeparator();
  }

  private void showErrorConsole() {
    this.errorDialog.center();
    this.errorDialog.show();
  }

  /**
   * Debugging functions.
   */
  private void displayStatus() {
    displayDebuggerUtilityTitle("ErraiBus Transport Status");

    final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();

    final boolean federatedApp = BusToolsCli.isRemoteCommunicationEnabled();

    nativeLog("Bus State               : " + (bus.getState()));
    nativeLog("Wire Protocol           : V3.JSON");
    nativeLog("Active Channel          : " + (!federatedApp ? "None" : (bus.getTransportHandler())));

    displaySeparator();
    final TransportStatistics stats = bus.getTransportHandler().getStatistics();

    nativeLog("Channel Details:");
    if (federatedApp) {
      nativeLog("  Channel Description   : " + (stats.getTransportDescription()));
      if (stats.isFullDuplex()) {
        nativeLog("  Endpoint (RX/TX)      : " + (stats.getRxEndpoint()));
      }
      else {
        nativeLog("  Endpoint (RX)         : " + (stats.getRxEndpoint()));
        nativeLog("  Endpoint (TX)         : " + (stats.getTxEndpoint()));
      }
      nativeLog("  Pending Transmissions : " + (stats.getPendingMessages()));
      nativeLog("");
      nativeLog("  TX Count              : " + (stats.getMessagesSent()));
      nativeLog("  RX Count              : " + (stats.getMessagesReceived()));
      final long connectedTime = stats.getConnectedTime();
      if (connectedTime == -1) {
        nativeLog("  Time Connected        : Not Connected.");
      }
      else {
        nativeLog("  Time Connected        : " + ((System.currentTimeMillis() - connectedTime) / 1000) + " secs.");
      }
      nativeLog("  Last Activity (TX/RX) : " + ((System.currentTimeMillis() - stats.getLastTransmissionTime()) / 1000) + " secs ago.");
      final int measuredLatency = stats.getMeasuredLatency();
      nativeLog("  Measured Latency      : " + (measuredLatency == -1 ? "N/A" : measuredLatency + "ms"));
    }
    else {
      nativeLog("  <No transport configured>");
    }

    displaySeparator();

    nativeLog("Available Handlers:");
    final Collection<TransportHandler> allAvailableHandlers = bus.getAllAvailableHandlers();

    if (allAvailableHandlers.isEmpty()) {
      nativeLog(" [none]");
    }
    for (final TransportHandler handler : allAvailableHandlers) {
      if (handler.isUsable()) {
        nativeLog("  > " + handler.getStatistics().getTransportDescription() + " " + (handler == bus.getTransportHandler() ? "**" : ""));
      }
    }
    nativeLog("Unavailable Handlers");
    for (final TransportHandler handler : allAvailableHandlers) {
      if (!handler.isUsable()) {
        nativeLog("  > " + handler.getStatistics().getTransportDescription() + " [reason: " + handler.getStatistics().getUnsupportedDescription() + "]");
      }
    }

    displaySeparator();

    nativeLog("Note: RX and TX counts are network events, not individual messages.");
    displaySeparator();
  }


}
