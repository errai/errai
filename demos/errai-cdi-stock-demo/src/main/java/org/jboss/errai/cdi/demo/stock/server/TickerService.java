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
package org.jboss.errai.cdi.demo.stock.server;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.demo.stock.client.shared.SubscriptionReply;
import org.jboss.errai.cdi.demo.stock.client.shared.SubscriptionRequest;
import org.jboss.errai.cdi.demo.stock.client.shared.Tick;
import org.jboss.errai.cdi.demo.stock.client.shared.TickBuilder;
import org.jboss.errai.cdi.demo.stock.client.shared.TickCache;

/**
 * Broadcasts fake price movements to clients.
 */
@ApplicationScoped
public class TickerService {

  @Inject
  private Event<Tick> tickEvent;

  @Inject
  Event<SubscriptionReply> subscriptionEvent;

  /** Scheduler that causes stock price updates. */
  private final ScheduledExecutorService tickGenerator = Executors.newScheduledThreadPool(1);

  /** The latest tick for each stock we care about. In each map entry, key == value.getSymbol(). */
  private final Map<String, TickCache> tickCaches = new LinkedHashMap<String, TickCache>();

  /** Source of randomness for initial state as well as subsequent ticks. */
  private final Random random = new Random();

  /**
   * Clients have to contact this service before they can receive events from it. In fact, the service doesn't get
   * created until the first client contacts it.
   *
   * @param subscription
   *          Details of the subscription request. Currently ignored.
   */
  public void handleClientSubscription(@Observes SubscriptionRequest subscription) {
    System.out.println("Got a client subscription");

    List<TickCache> histories = new ArrayList<TickCache>();

    // FIXME thread safety problem here. should synchronize on the tick caches here and in the ticker.
    for (Map.Entry<String, TickCache> tickEntry : tickCaches.entrySet()) {
      TickCache tickCache = tickEntry.getValue();
      histories.add(tickCache);
    }

    SubscriptionReply reply = new SubscriptionReply();
    reply.setTickHistories(histories);
    subscriptionEvent.fire(reply);
  }

  @PostConstruct
  public void startTicker() {

    // bootstrap the list of symbols
    String[] symbols = new String[] { "ABC", "DEF", "GHI", "JKL", "MNO", "PQR", "STU.V", "WXY.Z" };
    for (String symbol : symbols) {
      TickCache tickCache = new TickCache(new LinkedBlockingQueue<Tick>());
      tickCache.add(generateBootstrapTick(symbol));
      tickCaches.put(symbol, tickCache);
    }

    tickGenerator.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          generateTicks();
        }
        catch (Throwable e) {
          System.out.println("blam");
          e.printStackTrace();
        }
      }
    }, 0, 250, TimeUnit.MILLISECONDS);
  }

  private void generateTicks() {
    for (Map.Entry<String, TickCache> tickEntry : tickCaches.entrySet()) {
      if (random.nextInt(1000) > 200) {
        continue;
      }
      TickCache tickCache = tickEntry.getValue();
      Tick newTick = generateTick(tickCache.getNewestEntry());
      tickCache.add(newTick);
      tickEvent.fire(newTick);
    }
  }

  private Tick generateTick(Tick oldTick) {
    BigDecimal change = new BigDecimal(random.nextGaussian() * (oldTick.getPrice().doubleValue() / 1000), FOUR_DIGITS_PRECISION);
    TickBuilder newTick = new TickBuilder(oldTick.getSymbol())
      .time(new Date())
      .price(oldTick.getPrice().add(change))
      .change(change);
    return newTick.toTick();
  }

  private static final MathContext FOUR_DIGITS_PRECISION = new MathContext(4);

  private Tick generateBootstrapTick(String symbol) {
    return new TickBuilder(symbol)
      .time(new Date())
      .price(new BigDecimal(random.nextDouble() * 150, FOUR_DIGITS_PRECISION))
      .toTick();
  }
}
