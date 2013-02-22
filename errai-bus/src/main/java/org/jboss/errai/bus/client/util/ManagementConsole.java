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

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.common.client.util.LogUtil;

/**
 * @author Mike Brock
 */
public class ManagementConsole {
  private final ClientMessageBusImpl clientMessageBus;
  private BusErrorDialog errorDialog;

  public ManagementConsole(ClientMessageBusImpl clientMessageBus) {
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
      LogUtil.nativeLog(message);
    }
  }


  private native void declareDebugFunction() /*-{
      var thisRef = this;

      $wnd.errai_status = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::displayStatus();
      };

      $wnd.errai_list_services = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::listServices()();
      };

      $wnd.errai_show_error_console = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::showErrorConsole()();
      }
  }-*/;

  private void listServices() {

    LogUtil.displayDebuggerUtilityTitle("Service and Routing Table");
    LogUtil.nativeLog("[REMOTES]");

    for (final String remoteName : clientMessageBus.getRemoteServices()) {
      LogUtil.nativeLog(remoteName);
    }

    LogUtil.nativeLog("[LOCALS]");

    for (final String localName : clientMessageBus.getLocalServices()) {
      LogUtil.nativeLog(localName);
    }

    LogUtil.displaySeparator();
  }

  private void showErrorConsole() {
    this.errorDialog.center();
    this.errorDialog.show();
  }

  /**
   * Debugging functions.
   */
  private void displayStatus() {

    LogUtil.displayDebuggerUtilityTitle("ErraiBus Status");

    final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();
//    LogUtil.nativeLog("Bus State              : " + (bus.initialized ? "Online/Federated" : "Disconnected"));
    LogUtil.nativeLog("");
//    LogUtil.nativeLog("Comet Channel          : " + (bus.cometChannelOpen ? "Active" : "Offline"));
//    LogUtil.nativeLog("  Endpoint (RX)        : " + getApplicationRoot() + bus.IN_SERVICE_ENTRY_POINT);
//    LogUtil.nativeLog("  Endpoint (TX)        : " + getApplicationRoot() + bus.OUT_SERVICE_ENTRY_POINT);
//    LogUtil.nativeLog("  Pending Requests     : " + bus.pendingRequests.size());
//    LogUtil.nativeLog("");
//    LogUtil.nativeLog("WebSocket Channel      : " + (bus.webSocketOpen ? "Active" : "Offline"));
//    LogUtil.nativeLog("  Endpoint (RX/TX)     : " + bus.webSocketUrl);
//    LogUtil.nativeLog("");
//    LogUtil.nativeLog("Total TXs              : " + bus.txNumber);
//    LogUtil.nativeLog("Total RXs              : " + bus.rxNumber);
//    LogUtil.nativeLog("");
//    LogUtil.nativeLog("Endpoints");
//    LogUtil.nativeLog("  Remote (total)       : " + clientMessageBus.ge.size());
//    LogUtil.nativeLog("  Local (total)        : " + bus.subscriptions.size());

    LogUtil.displaySeparator();
  }
}
