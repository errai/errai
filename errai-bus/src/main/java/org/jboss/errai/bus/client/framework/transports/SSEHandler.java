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

package org.jboss.errai.bus.client.framework.transports;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.framework.BusState;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;

/**
 * An ErraiBus transport handler using server-sent events. It relies on 
 * {@link HttpPollingHandler} for transmitting messages.
 * 
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SSEHandler implements TransportHandler, TransportStatistics {
  private static final String SSE_AGENT_SERVICE = "SSEAgent";

  private final ClientMessageBusImpl clientMessageBus;

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

  private final Timer pingTimeout = new Timer() {
    @Override
    public void run() {
      if (!connected) {
        logger.warn(this + ": initial timeout expired");
        notifyDisconnected();
      }
    }
  };

  private Object sseChannel;

  /**
   * Bus subscription that receives ping responses from the server bus. This is
   * used for verifying that the SSE channel is actually working.
   */
  private final Subscription sseAgentSubscription;
  
  private static final Logger logger = LoggerFactory.getLogger(SSEHandler.class);

  public SSEHandler(final ClientMessageBusImpl clientMessageBus) {
    this.clientMessageBus = clientMessageBus;
    this.pollingHandler = HttpPollingHandler.newNoPollingInstance(clientMessageBus);

    sseAgentSubscription = clientMessageBus.subscribe(SSE_AGENT_SERVICE, new MessageCallback() {
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
      logger.warn("this browser does not support SSE");
      return;
    }

    this.sseEntryPoint = URL.encode(clientMessageBus.getApplicationLocation(clientMessageBus.getInServiceEntryPoint()))
        + "?&sse=1&clientId=" + URL.encodePathSegment(clientMessageBus.getClientId());

  }

  @Override
  public void start() {
    stopped = false;
    if (connected) {
      logger.info("did not start SSE handler: already started.");
      return;
    }
    sseChannel = attemptSSEChannel(clientMessageBus, sseEntryPoint + "&z=" + retries);
  }

  @Override
  public Collection<Message> stop(final boolean stopAllCurrentRequests) {
    stopped = true;
    if (sseChannel != null) {
      disconnect(sseChannel);
      sseChannel = null;
    }
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
    BusToolsCli.decodeToCallback(json, clientMessageBus);
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
          $wnd.console.log("SSE channel error (according to the browser)");
          $wnd.console.log(e);
          thisRef.@org.jboss.errai.bus.client.framework.transports.SSEHandler::notifyDisconnected()();
      };

      var openHandler = function () {
          $wnd.console.log("SSE channel opened (according to the browser)");
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

  /**
   * Sends a ping request to the server. If the ping response is not received
   * within a reasonable time limit, notifyDisconnected() will be called.
   */
  private void verifyConnected() {

    // in case we were in the middle of something already
    pingTimeout.cancel();
    
    transmit(Collections.singletonList(MessageBuilder.createMessage()
        .toSubject("ServerEchoService")
        .signalling().done().repliesToSubject(SSE_AGENT_SERVICE).getMessage()));

    pingTimeout.schedule(2500);
  }

  private void notifyConnected() {
    pingTimeout.cancel();
    retries = 0;

    if (!connected) {
      connected = true;
      connectedTime = System.currentTimeMillis();
      logger.info(this + ": SSE channel is active.");
    }

    if (clientMessageBus.getState() == BusState.CONNECTION_INTERRUPTED) {
      clientMessageBus.setState(BusState.CONNECTED);
    }
  }

  private void notifyDisconnected() {
    connected = false;

    pingTimeout.cancel();
    logger.info(this + " channel disconnected.");
    connectedTime = -1;
    clientMessageBus.setState(BusState.CONNECTION_INTERRUPTED);

    disconnect(sseChannel);

    if (!stopped) {
      if (retries == 0) {
        transmit(Collections.singletonList(MessageBuilder.createMessage()
              .toSubject("ServerEchoService")
              .signalling().done().repliesToSubject(SSE_AGENT_SERVICE).getMessage()));
      }
      
      final int retryDelay = Math.min((retries * 1000) + 1, 10000);
      logger.info("attempting SSE reconnection in " + retryDelay + "ms -- attempt: " + (++retries));
      
      new Timer() {
        @Override
        public void run() {
          if (!stopped) {
            start();
          }
        }
      }.schedule(retryDelay);
    }
  }

  @Override
  public String toString() {
    return "SSE[" + System.identityHashCode(this) + "]";
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

  @Override
  public void close() {
    if (!stopped) {
      stop(true);
    }
    sseAgentSubscription.remove();
    configured = false;
  }
}
