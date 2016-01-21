/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.util;

import java.util.Collection;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.transports.TransportHandler;
import org.jboss.errai.bus.client.framework.transports.TransportStatistics;
import org.jboss.errai.common.client.logging.formatters.ErraiSimpleFormatter;
import org.jboss.errai.common.client.logging.handlers.ErraiConsoleLogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 */
public class ManagementConsole {
  private final ClientMessageBusImpl clientMessageBus;
  private BusErrorDialog errorDialog;
  private final Logger logger = LoggerFactory.getLogger(ManagementConsole.class);

  private static final String SEP = "-------------------------------------------------------------------";

  public ManagementConsole(final ClientMessageBusImpl clientMessageBus) {
    this.clientMessageBus = clientMessageBus;
    this.errorDialog = new BusErrorDialog(clientMessageBus);

    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ManagementConsole.class.getName());
    ErraiSimpleFormatter esf = new ErraiSimpleFormatter("%5$s");
    ErraiConsoleLogHandler eclh = new ErraiConsoleLogHandler(esf);
    logger.addHandler(eclh);
    logger.setUseParentHandlers(false);

    declareDebugFunction();
  }

  public void displayError(final String message, final String additionalDetails, final Throwable e) {
    errorDialog.addError(message, additionalDetails, e);

    logger.error(message, e);
    logger.debug(additionalDetails, e);
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

      $wnd.errai_bus_stop = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::stopBus()();
      }

      $wnd.errai_bus_start = function () {
          thisRef.@org.jboss.errai.bus.client.util.ManagementConsole::startBus()();
      }
  }-*/;

  private void listServices() {

    displayUtilityTitle("Service and Routing Table");
    logger.info("[REMOTES]");

    for (final String remoteName : clientMessageBus.getRemoteServices()) {
      logger.info(remoteName);
    }

    logger.info("[LOCALS]");

    for (final String localName : clientMessageBus.getLocalServices()) {
      logger.info(localName);
    }

    logger.info(SEP);
  }

  private void showErrorConsole() {
    this.errorDialog.center();
    this.errorDialog.show();
  }

  /**
   * Debugging functions.
   */
  private void displayStatus() {
    displayUtilityTitle("ErraiBus Transport Status");

    final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();

    final boolean federatedApp = BusToolsCli.isRemoteCommunicationEnabled();

    logger.info("Bus State               : " + (bus.getState()));
    logger.info("Wire Protocol           : V3.JSON");
    logger.info("Active Channel          : " + (!federatedApp ? "None" : (bus.getTransportHandler())));

    logger.info(SEP);
    final TransportStatistics stats = bus.getTransportHandler().getStatistics();

    logger.info("Channel Details:");
    if (federatedApp) {
      logger.info("  Channel Description   : " + (stats.getTransportDescription()));
      if (stats.isFullDuplex()) {
        logger.info("  Endpoint (RX/TX)      : " + (stats.getRxEndpoint()));
      }
      else {
        logger.info("  Endpoint (RX)         : " + (stats.getRxEndpoint()));
        logger.info("  Endpoint (TX)         : " + (stats.getTxEndpoint()));
      }
      logger.info("  Pending Transmissions : " + (stats.getPendingMessages()));
      logger.info("");
      logger.info("  TX Count              : " + (stats.getMessagesSent()));
      logger.info("  RX Count              : " + (stats.getMessagesReceived()));
      final long connectedTime = stats.getConnectedTime();
      if (connectedTime == -1) {
        logger.info("  Time Connected        : Not Connected.");
      }
      else {
        logger.info("  Time Connected        : " + ((System.currentTimeMillis() - connectedTime) / 1000) + " secs.");
      }
      logger.info("  Last Activity (TX/RX) : " + ((System.currentTimeMillis() - stats.getLastTransmissionTime()) / 1000) + " secs ago.");
      final int measuredLatency = stats.getMeasuredLatency();
      logger.info("  Measured Latency      : " + (measuredLatency == -1 ? "N/A" : measuredLatency + "ms"));
    }
    else {
      logger.info("  <No transport configured>");
    }

    logger.info(SEP);

    logger.info("Available Handlers:");
    final Collection<TransportHandler> allAvailableHandlers = bus.getAllAvailableHandlers();

    if (allAvailableHandlers.isEmpty()) {
      logger.info(" [none]");
    }
    for (final TransportHandler handler : allAvailableHandlers) {
      if (handler.isUsable()) {
        logger.info("  > " + handler.getStatistics().getTransportDescription() + " " + (handler == bus.getTransportHandler() ? "**" : ""));
      }
    }
    logger.info("Unavailable Handlers");
    for (final TransportHandler handler : allAvailableHandlers) {
      if (!handler.isUsable()) {
        logger.info("  > " + handler.getStatistics().getTransportDescription() + " [reason: " + handler.getStatistics().getUnsupportedDescription() + "]");
      }
    }

    logger.info(SEP);

    logger.info("Note: RX and TX counts are network events, not individual messages.");
    logger.info(SEP);
  }

  private void stopBus() {
    clientMessageBus.stop(false);
  }

  private void startBus() {
    clientMessageBus.init();
  }

  private void displayUtilityTitle(final String title) {
    logger.info(title);
    logger.info(SEP);
  }

}
