/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests;

import java.util.IdentityHashMap;
import java.util.Map;

import org.jboss.errai.bus.client.api.RetryInfo;
import org.jboss.errai.bus.client.api.TransportError;
import org.jboss.errai.bus.client.api.TransportErrorHandler;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.framework.transports.TransportHandler;
import org.jboss.errai.common.client.api.Assert;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;

public class BusReconnectBackoffTest extends AbstractErraiTest {

  private TransportErrorCounter transportErrorCounter;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    transportErrorCounter = new TransportErrorCounter();
    bus.addTransportErrorHandler(transportErrorCounter);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    bus.removeTransportErrorHandler(transportErrorCounter);
    Runnable tearDown = new Runnable() {
      @Override
      public void run() {
      }
    };
    changeSimulatedErrorCode(0, tearDown);
    super.gwtTearDown();
  }

  public void testBusReconnectFrequencyWhenIdle() {
    runAfterInit(90000, new Runnable() {
      @Override
      public void run() {
        Runnable test = new Runnable() {
          @Override
          public void run() {
            final long startTime = System.currentTimeMillis();
    
            new Timer() {
    
              @Override
              public void run() {
                final int totalRetries = transportErrorCounter.getTotalRetryCount();
                final long elapsedClockTime = System.currentTimeMillis() - startTime;
    
                double retriesPerSecond = totalRetries / (elapsedClockTime / 1000.0);
    
                System.out.println(transportErrorCounter);
                System.out.println(elapsedClockTime + "ms elapsed; retries per second is now " + retriesPerSecond);
                assertTrue(retriesPerSecond < 3);
    
                if (elapsedClockTime > 16000) {
                  finishTest();
                  cancel();
                }
              }
            }.scheduleRepeating(2000);
          }
        };
        changeSimulatedErrorCode(404, test);
      }
    });
  }

  public void testBusReconnectFrequencyWhenSending() {
    runAfterInit(90000, new Runnable() {
      @Override
      public void run() {
        Runnable test = new Runnable() {
          public void run() {
            MessageBuilder.createMessage().toSubject("TestService3").done()
            .repliesTo(new MessageCallback() {
    
              @Override
              public void callback(Message message) {
                System.out.println("I just totally got a " + message);
                finishTest();
              }
            }).sendNowWith(bus);
    
            final long startTime = System.currentTimeMillis();
    
            new Timer() {
    
              @Override
              public void run() {
                final int totalRetries = transportErrorCounter.getTotalRetryCount();
                final long elapsedClockTime = System.currentTimeMillis() - startTime;
    
                double retriesPerSecond = totalRetries / (elapsedClockTime / 1000.0);
    
                System.out.println(transportErrorCounter);
                System.out.println(elapsedClockTime + "ms elapsed; retries per second is now " + retriesPerSecond);
    
                // NOTE TO MAINTAINERS: Hi! If this assertion fails, it could be
                // because throttling is broken, but it might also be because
                // transport handlers left over from the previous test have not been
                // shut down properly, when the bus was reset between tests. Or they
                // have been shut down but their stop() method isn't entirely
                // effective. Especially suspect would be pending timers that don't
                // get canceled when you call stop().
                assertTrue("Retries per second is now " + retriesPerSecond, retriesPerSecond < 3);
    
                if (elapsedClockTime > 16000) {
                  finishTest();
                  cancel();
                }
              }
            }.scheduleRepeating(2000);
          }
        };
        changeSimulatedErrorCode(404, test);
      }
    });
  }

  /**
   * Sends a request to the server which sets the servlet filter to
   * respond to further requests with the given HTTP status code.
   *
   * @param newCode
   *          The HTTP status code that will be sent in response to all
   *          subsequent ErraiBus requests.
   * @para runnable
   *          The runnable to execute after the response was received. 
   */
  private void changeSimulatedErrorCode(int newCode, final Runnable runnable) {
    RequestBuilder request = new RequestBuilder(RequestBuilder.GET, "errorSimulator.erraiBus?errorCode=" + newCode);
    try {
      request.setCallback(new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          runnable.run();
        }

        @Override
        public void onError(Request request, Throwable exception) {
          throw new RuntimeException(exception);
        }
      });
      request.send();
    } 
    catch (RequestException e) {
      throw new RuntimeException(e);
    }
  }

  private class TransportErrorCounter implements TransportErrorHandler {

    /**
     * The bus uses more than one transport handler at a time, and RetryInfo
     * can't be accumulated without knowledge of prior state. So we accumulate
     * retry data by transport.
     */
    private final Map<TransportHandler, StatsForHandler> statsPerHandler =
            new IdentityHashMap<TransportHandler, BusReconnectBackoffTest.TransportErrorCounter.StatsForHandler>();

    class StatsForHandler {
      final TransportHandler handler;
      long totalTimeBetweenRetries = 0;
      int retries = 0;
      int lastRetryCount = 0;

      public StatsForHandler(TransportHandler handler) {
        this.handler = Assert.notNull(handler);
      }

      void accumulate(TransportError error) {
        if (error.getSource() != handler) {
          throw new IllegalArgumentException("wrong handler. " + error.getSource() + " != " + handler);
        }
        RetryInfo retryInfo = error.getRetryInfo();
        totalTimeBetweenRetries += Math.max(0, retryInfo.getDelayUntilNextRetry());
        int retryCount = retryInfo.getRetryCount();
        if (retryCount == 1 || retryCount == lastRetryCount + 1) {
          retries++;
        }
        else {
          retries += retryCount;
        }
        lastRetryCount = retryCount;
      }

      public long getTotalTimeBetweenRetries() {
        return totalTimeBetweenRetries;
      }

      public int getRetries() {
        return retries;
      }

      public int getLastRetryCount() {
        return lastRetryCount;
      }

      @Override
      public String toString() {
        return "\nStatsForHandler [handler=" + handler + "@" + System.identityHashCode(handler)
                + ", totalTimeBetweenRetries=" + totalTimeBetweenRetries
                + ", retries=" + retries + ", lastRetryCount=" + lastRetryCount
                + "]";
      }

    }

    @Override
    public void onError(TransportError error) {
      TransportHandler source = error.getSource();
      StatsForHandler stats = statsPerHandler.get(source);
      if (stats == null) {
        stats = new StatsForHandler(source);
        statsPerHandler.put(source, stats);
      }
      stats.accumulate(error);
    }

    /**
     * Returns the sum of the rety attempts made by all transport handlers that have produced errors.
     */
    public int getTotalRetryCount() {
      int total = 0;
      for (StatsForHandler stats : statsPerHandler.values()) {
        total += stats.getRetries();
      }
      return total;
    }

    /**
     * Returns the sum of the delay time between rety attempts made by all
     * transport handlers that have produced errors.
     */
    public long getTotalTimeBetweenRetries() {
      long total = 0;
      for (StatsForHandler stats : statsPerHandler.values()) {
        total += stats.getTotalTimeBetweenRetries();
      }
      return total;
    }

    @Override
    public String toString() {
      return "TransportErrorCounter [statsPerHandler=" + statsPerHandler + "]";
    }
  }
}
