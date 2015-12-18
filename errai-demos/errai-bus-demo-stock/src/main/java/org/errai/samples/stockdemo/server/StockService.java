/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.errai.samples.stockdemo.server;

import com.google.inject.Inject;

import org.errai.samples.stockdemo.client.shared.Stock;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.util.TimeUnit;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StockService {
    private Map<String, Stock> stocks = new HashMap<String, Stock>();
    private List<String> tickerList = new CopyOnWriteArrayList<String>();
    private volatile AsyncTask task;

    @Inject
    public StockService(final RequestDispatcher dispatcher, final MessageBus bus) {
        loadDefault();

        bus.addSubscribeListener(new SubscribeListener() {
            public void onSubscribe(SubscriptionEvent event) {
                if (event.getSubject().equals("StockClient")) {
                    if (task == null || task.isCancelled()) {
                        task = MessageBuilder.createMessage()
                            .toSubject("StockClient")
                            .command("PriceChange")
                            .withProvided("Data", new ResourceProvider<String>() {
                                public String get() {
                                    return simulateRandomChange();
                                }
                            })
                            .noErrorHandling()
                            .sendRepeatingWith(dispatcher, TimeUnit.MILLISECONDS, 50);
                    }
                }
            }
        });
    }

    @Command("Start")
    public void start(Message message) {
        for (Stock stock : stocks.values()) {
            MessageBuilder.createConversation(message)
                .toSubject("StockClient")
                .command("UpdateStockInfo")
                .with("Stock", stock)
                .noErrorHandling().reply();
        }
    }

    @Command("GetStockInfo")
    public void getStockInfo(Message message) {
        Stock stock = stocks.get(message.get(String.class, "Ticker"));

        MessageBuilder.createConversation(message)
            .toSubject("StockClient")
            .command("UpdateStockInfo")
            .with("Stock", stock)
            .noErrorHandling().reply();
    }

    public String simulateRandomChange() {
        /**
         * Randomly choose a stock to update.
         */
        final String ticker = tickerList.get((int) (Math.random() * 1000) % tickerList.size());

        final Stock stock = stocks.get(ticker);

        if (Math.random() > 0.5d) {
            double price = stock.getLastTrade();

            if (Math.random() > 0.85d) {
                price += Math.random() * 0.05;
            }
            else if (Math.random() < 0.15d) {
                price -= Math.random() * 0.05;
            }

            // bias Errai to grow, unfairly.
            if ("ERR".equals(ticker)) {
                if (Math.random() > 0.5d) {
                    price += 0.01;
                }
            }

            stock.setLastTrade(price);
        }

        double volume = stock.getVolume();
        volume += Math.random() * stock.getVolumeWeighting();
        stock.setVolume(volume);

        return ticker + ":" + stock.getLastTrade() + ":" + stock.getVolume();
    }

    public void addEquity(String ticker, String company, double lastTrade) {
        stocks.put(ticker, new Stock(ticker, company, lastTrade));
        tickerList.add(ticker);
    }

    public void loadDefault() {
        addEquity("ERR", "Errai", 130);
        addEquity("FUN", "FunCo", 10.28);
        addEquity("FOO", "Foobar Worldco", 8.3);
        addEquity("GWTC", "The GWT Company", 5.2);
        addEquity("FGC", "Fun Gaming Corporation", 19.3);
        addEquity("XXX", "Triple X", 40.2);
        addEquity("XY", "Manco", 78.10);
        addEquity("XX", "Womanco", 90.10);
        addEquity("UXBR", "Ultimate X-Ray Bridgeco", 25.1);
        addEquity("RD", "Red Dog Inc.", 9.10);
        addEquity("JFN", "Java Financial Ltd", 90.2);
    }

}
