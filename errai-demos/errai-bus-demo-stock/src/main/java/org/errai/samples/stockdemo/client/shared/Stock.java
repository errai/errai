/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.errai.samples.stockdemo.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Stock {
    private String ticker;
    private String companyName;
    private double openingPrice;
    private double lastTrade;
    private double volume;
    private double volumeWeighting;

    public Stock() {
    }

    public Stock(String ticker, String companyName, double lastTrade) {
        this.ticker = ticker;
        this.companyName = companyName;
        this.openingPrice = this.lastTrade = lastTrade;
        volumeWeighting = openingPrice / 300;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(double openingPrice) {
        this.openingPrice = openingPrice;
    }

    public double getLastTrade() {
        return lastTrade;
    }

    public void setLastTrade(double lastTrade) {
        this.lastTrade = lastTrade;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public void setVolumeWeighting(double volumeWeighting) {
        this.volumeWeighting = volumeWeighting;
    }

    public double getVolumeWeighting() {
        return volumeWeighting;
    }
}
