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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.RetryInfo;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.TransportIOException;
import org.jboss.errai.bus.client.framework.BusState;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class HttpPollingHandler implements TransportHandler {
  public static int THROTTLE_TIME_MS = 150;
  public static int POLL_FREQUENCY_MS = 500;

  private boolean configured;

  private final MessageCallback messageCallback;
  private final ClientMessageBusImpl messageBus;

  private final List<Message> heldMessages = new ArrayList<Message>();
  private final List<Message> undeliveredMessages = new ArrayList<Message>();

  private int txNumber = 0;
  private int rxNumber = 0;
  private long lastTx = System.currentTimeMillis();

  boolean txActive = false;
  boolean rxActive = false;

  int retries = 0;

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

  private HttpPollingHandler(final MessageCallback messageCallback, final ClientMessageBusImpl messageBus) {
    this.messageCallback = Assert.notNull(messageCallback);
    this.messageBus = messageBus;
  }

  public static HttpPollingHandler newLongPollingInstance(final MessageCallback messageCallback,
                                                          final ClientMessageBusImpl messageBus) {

    final HttpPollingHandler handler = new HttpPollingHandler(messageCallback, messageBus);
    handler.receiveCommCallback = handler.new LongPollRequestCallback();
    return handler;
  }

  public static HttpPollingHandler newShortPollingInstance(final MessageCallback messageCallback,
                                                           final ClientMessageBusImpl messageBus) {

    final HttpPollingHandler handler = new HttpPollingHandler(messageCallback, messageBus);
    handler.receiveCommCallback = handler.new ShortPollRequestCallback();
    return handler;
  }

  public static HttpPollingHandler newNoPollingInstance(final MessageCallback messageCallback,
                                                        final ClientMessageBusImpl messageBus) {

    final HttpPollingHandler handler = new HttpPollingHandler(messageCallback, messageBus);
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

  //TODO: reimplement throttling
  @Override
  public void transmit(final List<Message> txMessages) {
    if (txMessages.isEmpty()) return;

    if (throttleOutgoing()) {
      heldMessages.addAll(txMessages);
      return;
    }

    final List<Message> toSend = new ArrayList<Message>();
    toSend.addAll(heldMessages);
    heldMessages.clear();
    toSend.addAll(txMessages);

    undeliveredMessages.addAll(toSend);

    final String message = BusToolsCli.encodeMessages(toSend);

    try {
      txActive = true;
      final long startTime = System.currentTimeMillis();

      try {
        final RequestCallback callback = new RequestCallback() {
          int statusCode = 0;

          @Override
          public void onResponseReceived(final Request request, final Response response) {
            statusCode = response.getStatusCode();

            // Messages can be considered re-sendable on an expired session only if the request has been in-flight
            // for less than 2 seconds.
            if (statusCode == 200) {
              undeliveredMessages.removeAll(toSend);
            }
            else if (statusCode == 401) {
              if (System.currentTimeMillis() - startTime > 2000) {
                undeliveredMessages.removeAll(toSend);
              }
            }
            else if (statusCode >= 400) {
              final TransportIOException tioe
                  = new TransportIOException(response.getText(), response.getStatusCode(),
                  "Failure communicating with server");

              if (messageBus.handleTransportError(new BusTransportError(request, tioe, statusCode, RetryInfo.NO_RETRY))) {
                return;
              }

              LogUtil.log("connection problem. server returned status code: " + response.getStatusCode() + " ("
                  + response.getStatusText() + ")");

              for (final Message txM : txMessages) {
                messageBus.callErrorHandler(txM, tioe);
              }
              return;
            }

            BusToolsCli.decodeToCallback(response.getText(), messageCallback);
          }

          @Override
          public void onError(final Request request, final Throwable exception) {
            messageBus.handleTransportError(new BusTransportError(request, exception, statusCode, RetryInfo.NO_RETRY));

            for (final Message txM : txMessages) {
              if (txM.getErrorCallback() == null || txM.getErrorCallback().error(txM, exception)) {
                LogUtil.log("failed to communicate with remote bus: " + exception);
              }
            }
          }
        };

        sendPollingRequest(message, Collections.<String, String>emptyMap(), callback);
      }
      catch (Exception e) {
        for (final Message txM : txMessages) {
          messageBus.callErrorHandler(txM, e);
        }
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
        toSend = new ArrayList<Message>(heldMessages);
        heldMessages.clear();
      }

      request = sendPollingRequest(BusToolsCli.encodeMessages(toSend), Collections.<String, String>emptyMap(), receiveCommCallback);
    }
    catch (RequestTimeoutException e) {

      // don't call the error handler here; it will be fired in onError()

      receiveCommCallback.onError(null, e);
    }
    catch (Throwable t) {
      if (messageBus.handleTransportError(new BusTransportError(request, t, -1, RetryInfo.NO_RETRY))) {
        return;
      }

      DefaultErrorCallback.INSTANCE.error(null, t);
    }
  }

  @Override
  public Collection<Message> stop(final boolean stopAllCurrentRequests) {
    receiveCommCallback.cancel();
    try {
      if (stopAllCurrentRequests) {
        // Now stop all the in-flight XHRs
        for (final RxInfo r : pendingRequests) {
          r.getRequest().cancel();
        }
        pendingRequests.clear();

        final ArrayList<Message> messages = new ArrayList<Message>(undeliveredMessages);
        return messages;
      }
      else {
        return Collections.emptyList();
      }
    }
    finally {
      undeliveredMessages.clear();
    }
  }

  private class NoPollRequestCallback extends LongPollRequestCallback {
    private boolean onePoll = false;

    @Override
    public void schedule() {
      if (!onePoll) {
        onePoll = true;
        performPoll();
      }
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
      final int retryDelay = retries * 1000;
      final RetryInfo retryInfo = new RetryInfo(retryDelay, retries);
      final BusTransportError transportError = new BusTransportError(request, throwable, statusCode, retryInfo);

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
          LogUtil.log("attempting reconnection -- attempt: " + (retries + 1));
          retries++;

          new Timer() {
            @Override
            public void run() {
              performPoll();
            }
          }.schedule(retryDelay);

          return;

        default:
          // polling error is probably unrecoverable; go to local-only mode
          DefaultErrorCallback.INSTANCE.error(null, throwable);

          messageBus.handleTransportError(transportError);
          messageBus.reconsiderTransport();

      }
    }

    @Override
    public void onResponseReceived(final Request request, final Response response) {
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
          case 401:
            BusToolsCli.decodeToCallback(response.getText(), messageCallback);
            break;
          default:
            onError(request,
                new TransportIOException("unexpected response code: " + statusCode, statusCode, response
                    .getStatusText()), statusCode);
            return;
        }
      }

      if (messageBus.getState() == BusState.CONNECTION_INTERRUPTED)
        messageBus.setState(BusState.CONNECTED);

      retries = 0;

      BusToolsCli.decodeToCallback(response.getText(), messageCallback);

      schedule();
    }

    public void schedule() {
      if (canceled) {
        return;
      }

      new Timer() {
        @Override
        public void run() {
          performPoll();
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

    final String serviceEntryPoint;
    final Map<String, String> parmsMap;
    final boolean waitChannel;

    if (receiveCommCallback.canWait() && !rxActive) {
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

    return sendRequest(RequestBuilder.POST, serviceEntryPoint, payload, parmsMap, callback, waitChannel);
  }

  public Request sendRequest(
      final RequestBuilder.Method method,
      final String serviceEntryPoint,
      final String payload,
      final Map<String, String> extraParameters,
      final RequestCallback callback,
      final boolean isWaitingChannel) throws RequestException {
    rxActive = true;

    final StringBuilder extraParmsString = new StringBuilder();
    for (final Map.Entry<String, String> entry : extraParameters.entrySet()) {
      extraParmsString.append("&").append(
          URL.encodePathSegment(entry.getKey())).append("=").append(URL.encodePathSegment(entry.getValue())
      );
    }

    final Iterator<RxInfo> iterator = pendingRequests.iterator();
    while (iterator.hasNext()) {
      final RxInfo pendingRx = iterator.next();
      if (isWaitingChannel && pendingRx.getRequest().isPending() && pendingRx.isWaiting()) {
        return null;
      }

      if (!pendingRx.getRequest().isPending()) {
        iterator.remove();
      }
    }

    final RequestBuilder builder = new RequestBuilder(
        method,
        URL.encode(messageBus.getApplicationLocation(serviceEntryPoint)) + "?z=" + getNextRequestNumber()
            + "&clientId=" + URL.encodePathSegment(messageBus.getClientId()) + extraParmsString.toString()
    );
    builder.setHeader("Content-Type", "application/json; charset=utf-8");

    final RxInfo rxInfo = new RxInfo(System.currentTimeMillis(), isWaitingChannel);

    try {
      final Request request = builder.sendRequest(payload, new RequestCallback() {
        @Override
        public void onResponseReceived(final Request request, final Response response) {
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
      throw e;
    }
  }


  public int getNextRequestNumber() {
    if (txNumber == Integer.MAX_VALUE) {
      txNumber = 0;
    }
    return txNumber++;
  }

  private boolean throttleOutgoing() {
    return (System.currentTimeMillis() - lastTx) < THROTTLE_TIME_MS;
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

  public String toString() {
    if (receiveCommCallback instanceof NoPollRequestCallback) {
      return "NoPolling";
    }
    else if (receiveCommCallback instanceof ShortPollRequestCallback) {
      return "ShortPolling";
    }
    else {
      return "LongPolling";
    }
  }
}
