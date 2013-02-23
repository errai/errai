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

package org.jboss.errai.bus.client.framework.transports;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.BusState;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.common.client.util.LogUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
public class SSEHandler implements TransportHandler {
  private final ClientMessageBusImpl clientMessageBus;
  private final MessageCallback messageCallback;

  private final HttpPollingHandler pollingHandler;
  private String sseEntryPoint;

  private boolean stopped;
  private boolean connected;
  private int retries;

  private boolean configured;
  private boolean hosed;

  private Object sseChannel;

  public SSEHandler(MessageCallback messageCallback, ClientMessageBusImpl clientMessageBus) {
    this.clientMessageBus = clientMessageBus;
    this.messageCallback = messageCallback;
    this.pollingHandler = HttpPollingHandler.newNoPollingInstance(messageCallback, clientMessageBus);
  }

  @Override
  public void configure(Message capabilitiesMessage) {
    if (!isSSESupported()) {
      hosed = true;
      LogUtil.log("this browser does not support SSE");
      return;
    }

    this.sseEntryPoint = URL.encode(clientMessageBus.getApplicationLocation(clientMessageBus.getInServiceEntryPoint()))
        + "?z=0000&sse=1&clientId=" + URL.encodePathSegment(clientMessageBus.getClientId());

    configured = true;
  }

  @Override
  public void start() {
    stopped = false;
    if (connected) {
      return;
    }
    sseChannel = attemptSSEChannel(clientMessageBus, sseEntryPoint);
  }

  @Override
  public Collection<Message> stop(boolean stopAllCurrentRequests) {
    stopped = true;
    disconnect(sseChannel);
    sseChannel = null;
    return  pollingHandler.stop(stopAllCurrentRequests);
  }

  @Override
  public void transmit(List<Message> txMessages) {
    this.pollingHandler.transmit(txMessages);
  }

  @Override
  public void handleProtocolExtension(Message message) {
  }

  @Override
  public boolean isUsable() {
    return !hosed && configured;
  }

  private void handleReceived(final String json) {
    BusToolsCli.decodeToCallback(json, messageCallback);
  }

  private static native void disconnect(Object channel) /*-{
     channel.close();
  }-*/;

  private native boolean isSSESupported() /*-{
      return !!window.EventSource;
  }-*/;

  private native Object attemptSSEChannel(final ClientMessageBusImpl bus, final String sseAddress) /*-{
      var thisRef = this;

      var errorHandler = function (e) {
          if (e.srcElement.readyState === EventSource.CLOSED) {
                thisRef.@org.jboss.errai.bus.client.framework.transports.SSEHandler::notifyDisconnected()();
          }

      };

      var openHandler = function(e) {
         thisRef.@org.jboss.errai.bus.client.framework.transports.SSEHandler::notifyConnected()();
      }

      var sseSource = new EventSource(sseAddress);
        sseSource.addEventListener('message', function (e) {
            thisRef.@org.jboss.errai.bus.client.framework.transports.SSEHandler::handleReceived(Ljava/lang/String;)(e.data);
        }, false);

      sseSource.onerror = errorHandler;
      sseSource.onopen = openHandler;

      return sseSource;
  }-*/;

  private void notifyConnected() {
    connected = true;
    LogUtil.log("SSE channel opened.");
    retries = 0;

    if (clientMessageBus.getState() == BusState.CONNECTION_INTERRUPTED)
      clientMessageBus.setState(BusState.CONNECTED);
  }

  private void notifyDisconnected() {
    LogUtil.log("SSE channel disconnected.");
    clientMessageBus.setState(BusState.CONNECTION_INTERRUPTED);

    connected = false;
    disconnect(sseChannel);

    if (!stopped) {
      retries++;
      new Timer() {
        @Override
        public void run() {
          LogUtil.log("attempting reconnection ... ");

          pollingHandler.performPoll();
          start();
        }

      }.schedule(retries * 1000);
    }
  }

  @Override
  public String toString() {
    return "SSE";
  }
}
