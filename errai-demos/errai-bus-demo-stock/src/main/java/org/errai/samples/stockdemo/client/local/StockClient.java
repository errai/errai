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

package org.errai.samples.stockdemo.client.local;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import org.errai.samples.stockdemo.client.shared.Stock;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.errai.samples.stockdemo.client.local.EquityRenderer.newEquity;

public class StockClient implements EntryPoint {

    private Map<String, EquityRenderer> equities = new HashMap<String, EquityRenderer>();
    private FlexTable table = new FlexTable();
    private int rows = 0;

    public void onModuleLoad() {
        setupTable();
        loadDefault();

        ErraiBus.get().subscribe("StockClient",
            new MessageCallback() {
                public void callback(Message message) {
                    if ("PriceChange".equals(message.getCommandType())) {
                        String[] data = message.get(String.class, "Data").split(":");
                        EquityRenderer renderer = equities.get(data[0]);

                        if (renderer != null) {
                            renderer.setLastTrade(Double.parseDouble(data[1]));
                            renderer.setVolume(Double.parseDouble(data[2]));
                        }
                    }
                    else if ("UpdateStockInfo".equals(message.getCommandType())) {
                        Stock stock = message.get(Stock.class, "Stock");

                        if (stock != null) {
                            EquityRenderer renderer = equities.get(stock.getTicker());

                            renderer.setCompanyName(stock.getCompanyName());
                            renderer.setOpeningPrice(stock.getOpeningPrice());
                            renderer.setLastTrade(stock.getLastTrade());
                            renderer.setVolume(stock.getVolume());
                        }
                    }
                }
            });

        MessageBuilder.createMessage()
            .toSubject("StockService")
            .command("Start")
            .noErrorHandling().sendNowWith(ErraiBus.getDispatcher());

        RootPanel.get().add(table);
    }

    private void addEquity(String ticker) {
        equities.put(ticker, newEquity(table, rows++, ticker));
    }

    private void addTableRow() {
        table.insertRow(rows);

        for (int i = 0; i < 6; i++)
            table.addCell(rows);

        rows++;
    }

    private void setupTable() {
        addTableRow();

        table.setWidget(0, 0, boldedLabel("Ticker"));
        table.setWidget(0, 1, boldedLabel("Company"));
        table.setWidget(0, 2, boldedLabel("Vol."));
        table.setWidget(0, 3, boldedLabel("Open"));
        table.setWidget(0, 4, boldedLabel("Last"));
        table.setWidget(0, 5, boldedLabel("Chg"));

        table.setWidth("100%");
        table.getCellFormatter().setWidth(0, 0, "100px");
        table.getCellFormatter().setWidth(0, 1, "250px");
        table.getCellFormatter().setWidth(0, 2, "80px");
        table.getCellFormatter().setWidth(0, 3, "80px");
        table.getCellFormatter().setWidth(0, 4, "80px");
        table.getCellFormatter().setWidth(0, 5, "80px");

    }

    private void loadDefault() {
        addEquity("ERR");
        addEquity("FUN");
        addEquity("FOO");
        addEquity("GWTC");
        addEquity("FGC");
        addEquity("XXX");
        addEquity("XX");
        addEquity("XY");
        addEquity("UXBR");
        addEquity("RD");
        addEquity("JFN");
    }

    private Label boldedLabel(String text) {
        Label label = new Label(text);
        label.getElement().getStyle().setProperty("fontWeight", "bold");
        return label;
    }

}
