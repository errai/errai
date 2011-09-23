/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.demo.stock.client.local;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.demo.stock.client.shared.Subscription;
import org.jboss.errai.cdi.demo.stock.client.shared.TickBuilder;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Main application entry point.
 */
@EntryPoint
public class App {

  private final Label tickerLabel = new Label();

  @Inject
  private Event<Subscription> subscriptionEvent;
  
  @PostConstruct
  public void buildUI() {
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.add(tickerLabel);

    RootPanel.get().add(horizontalPanel);
    
    subscriptionEvent.fire(new Subscription());
  }

  public void tickHappened(@Observes TickBuilder tick) {
    try {
      tickerLabel.setText("New tick at " + new Date() + ": " + tick);
    } catch (Exception e) {
      tickerLabel.setText(e.toString());
    }
    Document document = RootPanel.getBodyElement().getOwnerDocument();
    
    // find our stock box, creating if necessary
    DivElement stockBoxDiv = (DivElement) document.getElementById("stockbox." + tick.getSymbol());
    if (stockBoxDiv == null) {
      DivElement prototype = (DivElement) document.getElementById("prototypeStockBox");
      stockBoxDiv = (DivElement) prototype.cloneNode(true);
      stockBoxDiv.setId("stockbox." + tick.getSymbol());
      RootPanel.getBodyElement().appendChild(stockBoxDiv);
    }
    
    // update the stock box with current tick data
    NodeList<Element> nl = stockBoxDiv.getElementsByTagName("span");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = nl.getItem(i);
      if (el.getClassName().contains("stockName")) {
        el.setInnerText(tick.getSymbol());
      }
      else if (el.getClassName().contains("bidAsk")) {
        el.setInnerText(tick.getFormattedBid() + "/" + tick.getFormattedAsk());
      }
      else if (el.getClassName().contains("change")) {
        el.setInnerText(tick.getFormattedChange());
      }
      else if (el.getClassName().contains("time")) {
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        el.setInnerText(format.format(tick.getTime()));
      }
    }
    
    // finally, update the chart
    updateChart(
        stockBoxDiv,
        tick.getSymbol(),
        tick.getTime().getTime(),
        tick.getAsk() * Math.pow(10, -tick.getDecimalPlaces()));
  }
  
  private native void updateChart(DivElement stockBoxDiv, String symbol, double time, double value) /*-{
    var $ = $wnd.jQuery;
    
    // we store the tick history as data on the element for several reasons:
    // * no need to reparse JSON on every chart update
    // * keeping it in a Java Map keyed on symbol requires horrible JavaScript code for accessing it
    // * the data is removed exactly when we can't use it anymore (because the stock info div is gone)
    var ticks = $(stockBoxDiv).data("tickData");
    if (ticks == null) {
      ticks = [];
      $(stockBoxDiv).data("tickData", ticks);
    }
    ticks.push([time, value]);
    
    // prune entries too old to be visible
    var startTime = time - 180 * 1000;
    while (ticks[0][0] < startTime) {
      ticks.shift();
    }

    var chartDiv = $(stockBoxDiv).find(".chart");
    $.plot(chartDiv, [ ticks ], { xaxis: { mode: "time", min: startTime, max: time } });
  }-*/;
}