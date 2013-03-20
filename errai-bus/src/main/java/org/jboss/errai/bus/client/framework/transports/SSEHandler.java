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
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
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
public class SSEHandler implements TransportHandler, TransportStatistics {
  private static final String SSE_AGENT_SERVICE = "SSEAgent";

  private final ClientMessageBusImpl clientMessageBus;
  private final MessageCallback messageCallback;

  private final HttpPollingHandler pollingHandler;
  private String sseEntryPoint;

  private int rxCount;
  private long connectedTime = -1;

  private boolean stopped;
  private boolean connected;
  private int retries;

  private boolean configured;
  private boolean hosed;

  private String unsupportedReason = UNSUPPORTED_MESSAGE_NO_SERVER_SUPPORT;

  private final Timer initialTimeoutTimer = new Timer() {
    @Override
    public void run() {
      if (!connected) {
        notifyDisconnected();
      }
    }
  };

  private Object sseChannel;

  public SSEHandler(final MessageCallback messageCallback, final ClientMessageBusImpl clientMessageBus) {
    this.clientMessageBus = clientMessageBus;
    this.messageCallback = messageCallback;
    this.pollingHandler = HttpPollingHandler.newNoPollingInstance(messageCallback, clientMessageBus);

    clientMessageBus.subscribe(SSE_AGENT_SERVICE, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        notifyConnected();
      }
    });
  }

  @Override
  public void configure(final Message capabilitiesMessage) {
    configured = true;

    if (!isSSESupported()) {
      hosed = true;
      unsupportedReason = UNSUPPORTED_MESSAGE_NO_SERVER_SUPPORT;
      LogUtil.log("this browser does not support SSE");
      return;
    }

    this.sseEntryPoint = URL.encode(clientMessageBus.getApplicationLocation(clientMessageBus.getInServiceEntryPoint()))
        + "?z=0000&sse=1&clientId=" + URL.encodePathSegment(clientMessageBus.getClientId());

  }

  @Override
  public void start() {
    stopped = false;
    if (connected) {
      LogUtil.log("did not start SSE handler: already started.");
      return;
    }
    sseChannel = attemptSSEChannel(clientMessageBus, sseEntryPoint);

    // time out after 2 seconds and attempt reconnect. (note: this is really to deal with a bug a firefox).
    initialTimeoutTimer.schedule(2500);
  }

  @Override
  public Collection<Message> stop(final boolean stopAllCurrentRequests) {
    stopped = true;
    disconnect(sseChannel);
    sseChannel = null;
    return pollingHandler.stop(stopAllCurrentRequests);
  }

  @Override
  public void transmit(final List<Message> txMessages) {
    this.pollingHandler.transmit(txMessages);
  }

  @Override
  public void handleProtocolExtension(final Message message) {
  }

  @Override
  public boolean isUsable() {
    return !hosed && configured;
  }

  private void handleReceived(final String json) {
    rxCount++;
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

      var openHandler = function () {
          thisRef.@org.jboss.errai.bus.client.framework.transports.SSEHandler::verifyConnected()();
      };

      var sseSource = new EventSource(sseAddress);
      sseSource.addEventListener('message', function (e) {
          thisRef.@org.jboss.errai.bus.client.framework.transports.SSEHandler::handleReceived(Ljava/lang/String;)(e.data);
      }, false);

      sseSource.onerror = errorHandler;
      sseSource.onopen = openHandler;

      return sseSource;
  }-*/;


  private void verifyConnected() {
    transmit(Collections.singletonList(MessageBuilder.createMessage()
        .toSubject("ServerEchoService")
        .signalling().done().repliesToSubject(SSE_AGENT_SERVICE).getMessage()));
  }

  private void notifyConnected() {
    if (!connected) {
      return;
    }
    connected = true;

    initialTimeoutTimer.cancel();
    connectedTime = System.currentTimeMillis();
    LogUtil.log("SSE channel opened.");
    retries = 0;

    if (clientMessageBus.getState() == BusState.CONNECTION_INTERRUPTED)
      clientMessageBus.setState(BusState.CONNECTED);
  }

  private void notifyDisconnected() {
    initialTimeoutTimer.cancel();
    LogUtil.log("SSE channel disconnected.");
    connectedTime = -1;
    clientMessageBus.setState(BusState.CONNECTION_INTERRUPTED);

    connected = false;
    disconnect(sseChannel);

    if (!stopped) {
      retries++;
      new Timer() {
        @Override
        public void run() {
          LogUtil.log("attempting reconnection ... ");

          transmit(Collections.singletonList(MessageBuilder.createMessage()
              .toSubject("SSEAgent")
              .signalling().done().repliesToSubject("ClientBus").getMessage()));

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

  @Override
  public TransportStatistics getStatistics() {
    return this;
  }

  @Override
  public String getTransportDescription() {
    return "HTTP + Server-Sent Events";
  }

  @Override
  public String getUnsupportedDescription() {
    return unsupportedReason;
  }

  @Override
  public int getMessagesSent() {
    return pollingHandler.getMessagesSent();
  }

  @Override
  public int getMessagesReceived() {
    return rxCount;
  }

  @Override
  public long getConnectedTime() {
    return connectedTime;
  }

  @Override
  public int getMeasuredLatency() {
    return pollingHandler.getMeasuredLatency();
  }

  @Override
  public long getLastTransmissionTime() {
    return pollingHandler.getLastTransmissionTime();
  }

  @Override
  public boolean isFullDuplex() {
    return false;
  }

  @Override
  public String getRxEndpoint() {
    return clientMessageBus.getInServiceEntryPoint();
  }

  @Override
  public String getTxEndpoint() {
    return clientMessageBus.getOutServiceEntryPoint();
  }

  @Override
  public int getPendingMessages() {
    return pollingHandler.getStatistics().getPendingMessages();
  }
}
