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

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class EquityRenderer {
    private FlexTable table;
    private int row;

    private String ticker;
    private Label tickerLabel = new Label();

    private String companyName;
    private Label companyNameLabel = new Label();

    private double openingPrice;
    private Label openingPriceLabel = new Label();

    private double lastTrade;
    private Label lastTradeLabel = new Label();

    private double volume;
    private Label volumeLabel = new Label();

    private Label changeLabel = new Label();

    private NumberFormat format = NumberFormat.getFormat("###,###.##");

    private EquityRenderer(FlexTable table, int row, String ticker, String companyName, double openingPrice, double lastTrade,
        double volume) {
        this.table = table;
        this.row = row;
        setTicker(ticker);
        setCompanyName(companyName);
        setOpeningPrice(openingPrice);
        setLastTrade(lastTrade);
        setVolume(volume);

        renderOut();
    }

    private void renderOut() {
        table.setWidget(row, 0, tickerLabel);
        table.setWidget(row, 1, companyNameLabel);
        table.setWidget(row, 2, volumeLabel);
        table.setWidget(row, 3, openingPriceLabel);
        table.setWidget(row, 4, lastTradeLabel);
        table.setWidget(row, 5, changeLabel);
    }

    public static EquityRenderer newEquity(FlexTable table, int row, String ticker) {
        return new EquityRenderer(table, row, ticker, null, 0, 0, 0);
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
        tickerLabel.setText(ticker);
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        companyNameLabel.setText(companyName);
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(double openingPrice) {
        this.openingPrice = openingPrice;
        openingPriceLabel.setText(format.format(openingPrice));
    }

    public double getLastTrade() {
        return lastTrade;
    }

    public void setLastTrade(double lastTrade) {
        if (lastTrade < this.lastTrade) {
            lastTradeLabel.getElement().getStyle().setProperty("backgroundColor", "red");
        }
        else if (lastTrade == this.lastTrade) {
            lastTradeLabel.getElement().getStyle().setProperty("backgroundColor", "transparent");
        }
        else {
            lastTradeLabel.getElement().getStyle().setProperty("backgroundColor", "green");
        }

        this.lastTrade = lastTrade;
        lastTradeLabel.setText(format.format(lastTrade));

        double change = lastTrade - openingPrice;

        if (change >= 0) {
            changeLabel.getElement().getStyle().setProperty("color", "green");
        }
        else {
            changeLabel.getElement().getStyle().setProperty("color", "red");
        }

        changeLabel.setText(format.format(lastTrade - openingPrice));
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
        volumeLabel.setText(format.format(volume));
    }
}
