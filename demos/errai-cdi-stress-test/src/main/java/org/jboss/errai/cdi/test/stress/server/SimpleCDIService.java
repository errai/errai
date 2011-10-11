/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.test.stress.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.test.stress.client.shared.ConfigurationRequest;
import org.jboss.errai.cdi.test.stress.client.shared.SubscriptionRequest;
import org.jboss.errai.cdi.test.stress.client.shared.SubscriptionResponse;
import org.jboss.errai.cdi.test.stress.client.shared.TickEvent;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;

/**
 * A very simple CDI based service.
 */
@ApplicationScoped
public class SimpleCDIService {

  /**
   * The ID of the next event that will be created.
   */
  private final AtomicInteger nextEventId = new AtomicInteger();
  
  @Inject
  private Event<TickEvent> tickEvent;

  @Inject
  private Event<SubscriptionResponse> responseEvent;

  @Inject
  private Event<ConfigurationRequest> configurationChangeEvent;
  
  private final ScheduledExecutorService tickMaker = Executors.newSingleThreadScheduledExecutor();

  /**
   * The ticker that is currently running. Gets set up and/or replaced in
   * {@link #applyConfiguration(ConfigurationRequest)}.
   */
  private ScheduledFuture<?> currentTicker;

  /**
   * The configuration most recently applied by
   * {@link #applyConfiguration(ConfigurationRequest)}. Although
   * {@link ConfigurationRequest} is a mutable type, this configuration object
   * should be treated as immutable.
   */
  private ConfigurationRequest currentConfiguration;
  
  @SuppressWarnings("unused")
  @PostConstruct
  private void setup() {
    ConfigurationRequest cr = new ConfigurationRequest();
    cr.setPayloadSize(0);
    cr.setMessageInterval(250);
    cr.setMessageCount(10);
    applyConfiguration(cr);
  }
  
  @Conversational
  public void handleSubscriptionRequest(@Observes SubscriptionRequest req) {
    long offset = System.currentTimeMillis() - req.getClientTimestamp();
    System.out.println("Got a subscription request. Client's time offset is " + offset + "ms.");
    responseEvent.fire(new SubscriptionResponse(System.currentTimeMillis()));
    configurationChangeEvent.fire(currentConfiguration);
  }
  
  public void applyConfiguration(@Observes final ConfigurationRequest config) {
    if (currentTicker != null) {
      currentTicker.cancel(false);
    }

    // Create the new payload
    StringBuilder sb = new StringBuilder(config.getPayloadSize());
    for (int i = 0; i < config.getPayloadSize(); i++) {
      sb.append((char) ('a' + i % 26));
    }
    final String payload = sb.toString();
    
    currentTicker = tickMaker.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          System.out.print("Firing " + config.getMessageCount() + " ticks... ");
          for (int i = 0; i < config.getMessageCount(); i++) {
            tickEvent.fire(new TickEvent(nextEventId.getAndIncrement(), System.currentTimeMillis(), payload));
          }
        } catch (Throwable t) {
          t.printStackTrace(System.out);
        } finally {
          System.out.println("done. (interrupted=" + Thread.currentThread().isInterrupted() + ")");
        }
      }
    }, 0, config.getMessageInterval(), TimeUnit.MILLISECONDS);
    
    new Thread() {
      public void run() {
        try {
          currentTicker.get();
        } catch (Throwable t) {
          System.out.println("Ticker exited with exception (trace follows):");
          t.printStackTrace(System.out);
        } finally {
          System.out.println("Ticker finished");
        }
      };
    }.start();
    
    currentConfiguration = config;
  }
}
