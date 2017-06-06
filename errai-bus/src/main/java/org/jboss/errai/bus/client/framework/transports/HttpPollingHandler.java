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

import static org.jboss.errai.common.client.framework.Constants.ERRAI_CSRF_TOKEN_HEADER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.http.client.Header;
import org.jboss.errai.bus.client.api.InvalidBusContentException;
import org.jboss.errai.bus.client.api.RetryInfo;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.TransportIOException;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.framework.BusState;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.common.client.framework.ClientCSRFTokenCache;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;

/**
 * @author Mike Brock
 */
public class HttpPollingHandler implements TransportHandler, TransportStatistics {
  public static int THROTTLE_TIME_MS = 175;
  public static int POLL_FREQUENCY_MS = 500;
  
  private static final Logger logger = LoggerFactory.getLogger(HttpPollingHandler.class);

  private boolean configured;

  private final ClientMessageBusImpl messageBus;

  private final List<Message> heldMessages = new ArrayList<Message>();
  private final List<Message> undeliveredMessages = new ArrayList<Message>();

  private int txNumber = 0;
  private int rxNumber = 0;

  private long connectedTime = -1;
  private long lastTx = System.currentTimeMillis();

  private int measuredLatency = -1;

  private final Timer throttleTimer = new Timer() {
    @Override
    public void run() {
      transmit(getDeferredToSend());
    }
  };

  /**
   * Set to true when an outbound transmission is in progress. This flag is designed to guard against more than
   * one transmission from happening at once.
   */
  boolean txActive = false;

  /**
   * Set to true when an inbound transmission (ie. a long poll) is in progress. This flag is designed to guard
   * against more than one transmission from happening at once.
   */
  boolean rxActive = false;

  int rxRetries = 0;
  int txRetries = 0;

  /**
   * Keeps track of all pending requests, both incoming (receive builders) and
   * outgoing (send builders). Entries in this set are normally responsible for
   * removing themselves when they get the success or error callback. However,
   * when we stop the bus on purpose, the stop() method explicitly cancels
   * everything in this set. In that case, the RequestCallbacks are never
   * invoked.
   */
  private final Set<RxInfo> pendingRequests = new LinkedHashSet<RxInfo>();

  /**
   * Note that this could be any subtype of LongPollRequestCallback, including
   * ShortPollRequestCallback or NoPollRequestCallback.
   * <p/>
   * Instance invariant: this field is never set to null.
   */
  private LongPollRequestCallback receiveCommCallback = new NoPollRequestCallback();

  private HttpPollingHandler(final ClientMessageBusImpl messageBus) {
    this.messageBus = messageBus;
  }

  public static HttpPollingHandler newLongPollingInstance(final ClientMessageBusImpl messageBus) {

    final HttpPollingHandler handler = new HttpPollingHandler(messageBus);
    handler.receiveCommCallback = handler.new LongPollRequestCallback();
    return handler;
  }

  public static HttpPollingHandler newShortPollingInstance(final ClientMessageBusImpl messageBus) {

    final HttpPollingHandler handler = new HttpPollingHandler(messageBus);
    handler.receiveCommCallback = handler.new ShortPollRequestCallback();
    return handler;
  }

  public static HttpPollingHandler newNoPollingInstance(final ClientMessageBusImpl messageBus) {

    final HttpPollingHandler handler = new HttpPollingHandler(messageBus);
    handler.receiveCommCallback = handler.new NoPollRequestCallback();
    return handler;
  }

  @Override
  public void configure(final Message capabilitiesMessage) {
    configured = true;
  }

  @Override
  public boolean isUsable() {
    return configured;
  }

  @Override
  public void start() {
    receiveCommCallback.schedule();
  }

  private boolean throttleMessages(final List<Message> txMessages) {
    final int window = throttleOutgoing();
    if (window <= 0) {
      heldMessages.addAll(txMessages);
      throttleTimer.schedule(-window);
      return true;
    }
    else {
      return false;
    }
  }

  private List<Message> getDeferredToSend() {
    throttleTimer.cancel();
    try {
      return new ArrayList<Message>(heldMessages);
    }
    finally {
      heldMessages.clear();
    }
  }

  @Override
  public void transmit(final List<Message> txMessages) {
    if (txRetries > 0) {
      heldMessages.addAll(txMessages);
    }
    else {
      transmit(txMessages, false);
    }
  }
  
  private void transmit(final List<Message> txMessages, boolean isRetry) {
    if (txMessages.isEmpty()) {
      return;
    }

    logger.trace("[bus] HttpPollingHandler.transmit(" + txMessages + ")");

    final List<Message> toSend = new ArrayList<Message>();

    boolean canDefer = !isRetry;
    final Map<String, String> specialParms;
    if (txMessages.size() == 1 && txMessages.get(0).hasResource(EXTRA_URI_PARMS_RESOURCE)) {
      toSend.add(txMessages.get(0));
      specialParms = txMessages.get(0).getResource(Map.class, EXTRA_URI_PARMS_RESOURCE);
    }
    else {
      for (final Message message : txMessages) {
        if (message.hasPart(MessageParts.PriorityProcessing)) {
          canDefer = false;
          break;
        }
        else if (message.hasResource(EXTRA_URI_PARMS_RESOURCE)) {
          throw new IllegalStateException("cannot send payload. messages with special URI parms must be sent one at a time.");
        }
      }

      if (canDefer && throttleMessages(txMessages)) {
        logger.trace("[bus] *DEFERRED* :: " + txMessages);
        return;
      }
      else {
        toSend.addAll(getDeferredToSend());
        toSend.addAll(txMessages);
      }

      specialParms = Collections.emptyMap();
    }

    undeliveredMessages.addAll(toSend);

    final String message = BusToolsCli.encodeMessages(toSend);
    logger.trace("[bus] toSend=" + toSend);

    try {
      txActive = true;
      final long startTime = System.currentTimeMillis();

      try {
        sendPollingRequest(message, specialParms, new RemoteRequestCallback(startTime, toSend, txMessages));
      }
      catch (Exception e) {
        e.printStackTrace();
        for (final Message txM : txMessages) {
          messageBus.callErrorHandler(txM, e);
        }
        logger.error("exception: " + e.getMessage(), e);
      }
    }
    finally {
      lastTx = System.currentTimeMillis();
      txActive = false;
    }
  }

  public void performPoll() {
    Request request = null;
    try {
      final List<Message> toSend;
      if (heldMessages.isEmpty()) {
        toSend = Collections.emptyList();
      }
      else {
        toSend = new ArrayList<Message>(getDeferredToSend());
      }

      request = sendPollingRequest(BusToolsCli.encodeMessages(toSend), Collections.<String, String>emptyMap(), receiveCommCallback);
    }
    catch (RequestTimeoutException e) {
      receiveCommCallback.onError(null, e);
    }
    catch (Throwable t) {
      if (messageBus.handleTransportError(new BusTransportError(this, request, t, -1, RetryInfo.NO_RETRY))) {
        return;
      }

      DefaultErrorCallback.INSTANCE.error(null, t);
    }
  }

  @Override
  public Collection<Message> stop(final boolean stopAllCurrentRequests) {
    receiveCommCallback.cancel();
    throttleTimer.cancel();
    
    try {
      if (stopAllCurrentRequests) {
        // Now stop all the in-flight XHRs
        for (final RxInfo r : pendingRequests) {
          r.getRequest().cancel();
        }
        pendingRequests.clear();

        return new ArrayList<Message>(undeliveredMessages);
      }
      else {
        return Collections.emptyList();
      }
    }
    finally {
      undeliveredMessages.clear();
    }
  }

  public boolean isCancelled() {
    return receiveCommCallback.canceled;
  }
  
  private class NoPollRequestCallback extends LongPollRequestCallback {

    @Override
    public void schedule() {
    }

    @Override
    public boolean canWait() {
      return false;
    }
  }

  private class ShortPollRequestCallback extends LongPollRequestCallback {
    public ShortPollRequestCallback() {
    }

    @Override
    public void schedule() {
      new Timer() {
        @Override
        public void run() {
          if (!canceled) {
            performPoll();
          }
        }
      }.schedule(POLL_FREQUENCY_MS);
    }

    @Override
    public boolean canWait() {
      return false;
    }
  }

  @Override
  public void handleProtocolExtension(final Message message) {
  }

  private static void validateContentType(final Response response) {
    int statusCode = response.getStatusCode();
    // in case the response is OK (200), then the content type always has to be 'application/json'
    if (statusCode == 200) {
      String contentType = response.getHeader("Content-Type");
      if (!contentType.contains("application/json")) {
        String content = response.getText();
        throw new InvalidBusContentException(contentType, content);
      }
    }
  }

  private class LongPollRequestCallback implements RequestCallback {
    /**
     * Subclasses MUST check this flag is still false before calling performPoll().
     */
    protected boolean canceled = false;

    @Override
    public void onError(final Request request, final Throwable throwable) {
      onError(request, throwable, -1);
    }

    public void onError(final Request request, final Throwable throwable, final int statusCode) {
      final int retryDelay = Math.min((rxRetries * 1000) + 1, 10000);

      final RetryInfo retryInfo = new RetryInfo(retryDelay, rxRetries);
      final BusTransportError transportError =
              new BusTransportError(HttpPollingHandler.this, request, throwable, statusCode, retryInfo);

      notifyDisconnected();

      if (messageBus.handleTransportError(transportError)) {
        return;
      }

      switch (statusCode) {
        case 0:
        case 1:
        case 401:
          break;
        case 400: // happens when JBossAS is going down
        case 404: // happens after errai app is undeployed
        case 408: // request timeout--probably worth retrying
        case 500: // we expect this may happen during restart of some non-JBoss servers
        case 502: // bad gateway--could happen if long poll request was proxied to a down server
        case 503: // temporary overload (probably on a proxy)
        case 504: // gateway timeout--same possibilities as 502
          logger.info("attempting Rx reconnection in " + retryDelay + "ms -- attempt: " + (rxRetries + 1));
          rxRetries++;

          throttleTimer.cancel();
          new Timer() {
            @Override
            public void run() {
              if (!canceled) {
                performPoll();
              }
            }
          }.schedule(retryDelay);

          return;

        default:
          // polling error is probably unrecoverable; go to local-only mode
          DefaultErrorCallback.INSTANCE.error(null, throwable);

          messageBus.handleTransportError(transportError);
      }
    }

    @Override
    public void onResponseReceived(final Request request, final Response response) {
      validateContentType(response);
      final int statusCode = response.getStatusCode();
      if (statusCode != 200) {
        switch (statusCode) {
          case 300:
          case 301:
          case 302:
          case 303:
          case 304:
          case 305:
          case 307:
            break;

          default:
            onError(request,
                new TransportIOException("unexpected response code: " + statusCode, statusCode, response
                    .getStatusText()), statusCode);
            return;
        }
      }

      notifyConnected();

      BusToolsCli.decodeToCallback(response.getText(), messageBus);

      if (pendingRequests.isEmpty()) {
        schedule();
      }
    }

    public void schedule() {
      if (canceled) {
        return;
      }

      new Timer() {
        @Override
        public void run() {
          if (!canceled) {
            performPoll();
          }
        }
      }.schedule(1);
    }

    public void cancel() {
      canceled = true;
    }

    public boolean canWait() {
      return true;
    }
  }

  /**
   * Sends the given string oon the outbound communication channel (as a POST
   * request to the server).
   *
   * @param payload
   *     The message to send. It is sent verbatim.
   * @param callback
   *     The callback to receive success or error notification. Note that
   *     this callback IS NOT CALLED if the request is cancelled.
   * @param extraParameters
   *     Extra paramets to include in the HTTP request (key is parameter name;
   *     value is parameter value).
   *
   * @throws com.google.gwt.http.client.RequestException
   *     if the request cannot be sent at all.
   */
  public Request sendPollingRequest(
      final String payload,
      final Map<String, String> extraParameters,
      final RequestCallback callback) throws RequestException {

    logger.trace("[bus] sendPollingRequest(" + payload + ")");

    final String serviceEntryPoint;
    final Map<String, String> parmsMap;
    final boolean waitChannel;
    boolean activeWaitChannel = false;

    final Iterator<RxInfo> iterator = pendingRequests.iterator();
      while (iterator.hasNext()) {
        final RxInfo pendingRx = iterator.next();
        if (pendingRx.getRequest().isPending() && pendingRx.isWaiting()) {
        //  LogUtil.log("[bus] ABORT SEND: " + pendingRx + " is waiting" );
         // return null;
          activeWaitChannel = true;
        }

        if (!pendingRx.getRequest().isPending()) {
          iterator.remove();
        }
      }

    if (!activeWaitChannel && receiveCommCallback.canWait() && !rxActive) {
      parmsMap = new HashMap<String, String>(extraParameters);
      parmsMap.put("wait", "1");
      serviceEntryPoint = messageBus.getInServiceEntryPoint();
      waitChannel = true;
    }
    else {
      parmsMap = extraParameters;
      serviceEntryPoint = messageBus.getOutServiceEntryPoint();
      waitChannel = false;
    }

    rxActive = true;

    final StringBuilder extraParmsString = new StringBuilder();
    for (final Map.Entry<String, String> entry : parmsMap.entrySet()) {
      extraParmsString.append("&").append(
          URL.encodePathSegment(entry.getKey())).append("=").append(URL.encodePathSegment(entry.getValue())
      );
    }

    final long latencyTime = System.currentTimeMillis();

    final RequestBuilder builder = new RequestBuilder(
        RequestBuilder.POST,
        URL.encode(messageBus.getApplicationLocation(serviceEntryPoint)) + "?z=" + getNextRequestNumber()
            + "&clientId=" + URL.encodePathSegment(messageBus.getClientId()) + extraParmsString.toString()
    );
    builder.setHeader("Content-Type", "application/json; charset=utf-8");
    // this has no effect on same origin requests but ensures the cookie and auth
    // headers are sent when using CORS.
    builder.setIncludeCredentials(true);
    if (ClientCSRFTokenCache.hasAssignedCSRFToken())
      builder.setHeader(ERRAI_CSRF_TOKEN_HEADER, ClientCSRFTokenCache.getAssignedCSRFToken());

    final RxInfo rxInfo = new RxInfo(System.currentTimeMillis(), waitChannel);

    try {
      logger.trace("[bus] TX: " + payload);
      final Request request = builder.sendRequest(payload, new RequestCallback() {
        @Override
        public void onResponseReceived(final Request request, final Response response) {
          if (!waitChannel) {
            measuredLatency = (int) (System.currentTimeMillis() - latencyTime);
          }

          pendingRequests.remove(rxInfo);
          callback.onResponseReceived(request, response);
          rxNumber++;
          rxActive = false;
        }

        @Override
        public void onError(final Request request, final Throwable exception) {
          pendingRequests.remove(rxInfo);
          callback.onError(request, exception);
          rxActive = false;
        }
      });

      rxInfo.setRequest(request);
      pendingRequests.add(rxInfo);

      return request;
    }
    catch (RequestException e) {
      pendingRequests.remove(rxInfo);
      throw e;
    }
  }

  public int getNextRequestNumber() {
    if (txNumber == Integer.MAX_VALUE) {
      txNumber = 0;
    }
    return txNumber++;
  }

  private int throttleOutgoing() {
    return (int) (System.currentTimeMillis() - lastTx) - THROTTLE_TIME_MS;
  }

  private void notifyConnected() {
    if (connectedTime == -1) {
      connectedTime = System.currentTimeMillis();
    }

    if (messageBus.getState() == BusState.CONNECTION_INTERRUPTED)
      messageBus.setState(BusState.CONNECTED);

    rxRetries = 0;
  }

  private void notifyDisconnected() {
    connectedTime = -1;
  }

  private static class RxInfo {
    private Request request;
    private final boolean waiting;
    private final long time;

    private RxInfo(long time, boolean waiting) {
      this.time = time;
      this.waiting = waiting;
    }

    public void setRequest(Request request) {
      this.request = request;
    }

    public Request getRequest() {
      return request;
    }

    public boolean isWaiting() {
      return waiting;
    }

    public long getTime() {
      return time;
    }
  }

  @Override
  public String toString() {
    if (receiveCommCallback instanceof NoPollRequestCallback) {
      return "No Polling";
    }
    else if (receiveCommCallback instanceof ShortPollRequestCallback) {
      return "Short Polling";
    }
    else {
      return "Long Polling";
    }
  }

  @Override
  public TransportStatistics getStatistics() {
    return this;
  }

  @Override
  public String getTransportDescription() {
    return "HTTP " + toString();
  }

  @Override
  public int getMessagesSent() {
    return txNumber;
  }

  @Override
  public int getMessagesReceived() {
    return rxNumber;
  }

  @Override
  public long getConnectedTime() {
    return connectedTime;
  }

  @Override
  public long getLastTransmissionTime() {
    return lastTx;
  }

  @Override
  public int getMeasuredLatency() {
    return measuredLatency;
  }

  @Override
  public boolean isFullDuplex() {
    return false;
  }

  @Override
  public String getRxEndpoint() {
    return messageBus.getInServiceEntryPoint();
  }

  @Override
  public String getTxEndpoint() {
    return messageBus.getOutServiceEntryPoint();
  }

  @Override
  public String getUnsupportedDescription() {
    return UNSUPPORTED_MESSAGE_NO_SERVER_SUPPORT;
  }

  @Override
  public int getPendingMessages() {
    return heldMessages.size();
  }

  private class RemoteRequestCallback implements RequestCallback {
    private final long startTime;
    private final List<Message> toSend;
    private final List<Message> txMessages;
    int statusCode;

    public RemoteRequestCallback(long startTime, List<Message> toSend, List<Message> txMessages) {
      this.startTime = startTime;
      this.toSend = toSend;
      this.txMessages = txMessages;
      statusCode = 0;
    }

    @Override
    public void onResponseReceived(final Request request, final Response response) {
      validateContentType(response);
      txActive = false;
      statusCode = response.getStatusCode();

      switch (statusCode) {
        case 401:
          if (System.currentTimeMillis() - startTime > 2000) {
            undeliveredMessages.removeAll(toSend);
          }
          try {
            if (BusToolsCli.decodeToCallback(response.getText(), messageBus)) {
              break;
            }
          }
          catch (Throwable e) {
             // fall through.
          }
        case 0:
        case 1:
        case 400: // happens when JBossAS is going down
        case 404: // happens after errai app is undeployed
        case 408: // request timeout--probably worth retrying
        case 500: // we expect this may happen during restart of some non-JBoss servers
        case 502: // bad gateway--could happen if long poll request was proxied to a down server
        case 503: // temporary overload (probably on a proxy)
        case 504: // gateway timeout--same possibilities as 502
        {
          final int retryDelay = Math.min((txRetries * 1000) + 1, 10000);
          final RetryInfo retryInfo = new RetryInfo(retryDelay, txRetries);
          final BusTransportError transportError =
                  new BusTransportError(HttpPollingHandler.this, request, null, statusCode, retryInfo);

          notifyDisconnected();

          if (messageBus.handleTransportError(transportError)) {
            return;
          }

          logger.info("attempting Tx reconnection in " + retryDelay + "ms -- attempt: " + (txRetries + 1));
          txRetries++;

          throttleTimer.cancel();
          new Timer() {
            @Override
            public void run() {
              undeliveredMessages.removeAll(toSend);
              transmit(toSend, true);
            }
          }.schedule(retryDelay);

          return;
        }
        case 200:
          txRetries = 0;
          notifyConnected();
          undeliveredMessages.removeAll(toSend);
          schedulePolling();
          break;

        // Happens for first bus request when CSRF token is enabled
        case 403: {
          final String assignedCSRFToken = response.getHeader(ERRAI_CSRF_TOKEN_HEADER);
          if (assignedCSRFToken != null) {
            ClientCSRFTokenCache.setAssignedCSRFToken(assignedCSRFToken);
            txRetries++;
            undeliveredMessages.removeAll(toSend);
            transmit(toSend, true);
            return;
          }
        }
        default: {
          final BusTransportError transportError =
                  new BusTransportError(HttpPollingHandler.this, request, null, statusCode, RetryInfo.NO_RETRY);

          if (messageBus.handleTransportError(transportError)) {
            return;
          }

          // polling error is probably unrecoverable; go to local-only mode
          DefaultErrorCallback.INSTANCE.error(null, null);

          messageBus.handleTransportError(transportError);
        }
      }
      // do not decode the payload in case of server returning an error code
      if (statusCode == 200) {
        BusToolsCli.decodeToCallback(response.getText(), messageBus);
      }
    }

    @Override
    public void onError(final Request request, final Throwable exception) {
      txActive = false;
      notifyDisconnected();

      messageBus.handleTransportError(
              new BusTransportError(HttpPollingHandler.this, request, exception, statusCode, RetryInfo.NO_RETRY));

      for (final Message txM : txMessages) {
        if (txM.getErrorCallback() == null || txM.getErrorCallback().error(txM, exception)) {
          logger.error("failed to communicate with remote bus.", exception);
        }
      }
      schedulePolling();
    }

    private void schedulePolling() {
      if (pendingRequests.isEmpty()) {
        new Timer() {
          @Override
          public void run() {
            performPoll();
          }
        }.schedule(1);
      }
    }
  }

  @Override
  public void close() {
    stop(true);
    configured = false;
  }
}
