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

import org.jboss.errai.cdi.demo.stock.client.shared.SubscriptionReply;
import org.jboss.errai.cdi.demo.stock.client.shared.SubscriptionRequest;
import org.jboss.errai.cdi.demo.stock.client.shared.Tick;
import org.jboss.errai.cdi.demo.stock.client.shared.TickCache;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
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
 * Main application entry point. Provides JavaScript code to the App.html page.
 */
@EntryPoint
public class App {

  /**
   * The amount of time each chart spans, in milliseconds. Gets populated in
   * {@link #subscriptionCompleted(SubscriptionReply)} based on initial snapshot size from the server.
   */
  private long chartTimeSpan;

  private final Label tickerLabel = new Label();

  /**
   * Indicates whether or not the registration message has been received and processed yet: it is possible that we will
   * receive ticks before the subscription reply, and that makes the order of things in the UI unpredictable.
   */
  private boolean registrationComplete = false;

  @Inject
  private Event<SubscriptionRequest> subscriptionEvent;

  @PostConstruct
  public void buildUI() {
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.add(tickerLabel);

    RootPanel.get().add(horizontalPanel);

    subscriptionEvent.fire(new SubscriptionRequest());
  }

  /**
   * Handles completion of the subscription request by creating all the stock info divs, pre-filling them with tick
   * history, and rendering their charts.
   *
   * @param subscriptionReply
   *          The subscription reply message from the server (contains tick history)
   */
  public void subscriptionCompleted(@Observes SubscriptionReply subscriptionReply) {
    for (TickCache cache : subscriptionReply.getTickHistories()) {
      chartTimeSpan = cache.getTimeSpan(); // XXX assumption is that all charts have same time span
      DivElement stockBoxDiv = getStockBoxDiv(cache.getNewestEntry());
      JsArray<JsArrayNumber> history = getChartData(stockBoxDiv);
      for (Tick t : cache) {
        addTick(history, t);
      }
      long endTime = cache.getNewestEntry().getTime().getTime();
      redrawChart(stockBoxDiv, endTime - chartTimeSpan, endTime);
    }
    registrationComplete = true;
  }

  /**
   * Handles a new tick from the server by updating the HTML UI.
   * <p>
   * This method doesn't do anything (it just returns immediately) until after
   * {@link #subscriptionCompleted(SubscriptionReply)} has been called.
   *
   * @param tick
   *          The tick that just happened
   */
  public void tickHappened(@Observes Tick tick) {
    if (!registrationComplete)
      return;
    try {
      tickerLabel.setText("New tick at " + new Date() + ": " + tick);
    }
    catch (Exception e) {
      tickerLabel.setText(e.toString());
    }
    DivElement stockBoxDiv = getStockBoxDiv(tick);

    addTick(getChartData(stockBoxDiv), tick);

    // update the stock box with current tick data
    NodeList<Element> nl = stockBoxDiv.getElementsByTagName("span");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = nl.getItem(i);
      if (el.getClassName().contains("stockName")) {
        el.setInnerText(tick.getSymbol());
      }
      else if (el.getClassName().contains("bidAsk")) {
        el.setInnerText(tick.getFormattedPrice());
      }
      else if (el.getClassName().contains("change")) {
        el.setInnerText(tick.getFormattedChange());
        String strobeCssColor = tick.getChange().signum() >= 0 ? "rgb(40, 155, 40)" : "rgb(155, 40, 40)";
        strobe(el, strobeCssColor, "rgb(0, 0, 0)");
      }
      else if (el.getClassName().contains("time")) {
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        el.setInnerText(format.format(tick.getTime()));
      }
    }

    // finally, update the chart
    double endTime = tick.getTime().getTime();
    double startTime = endTime - chartTimeSpan;
    redrawChart(stockBoxDiv, startTime, endTime);
  }

  /**
   * Strobes the foreground color of the given element. This feat is accomplished using jQuery's animate() method
   * together with the jquery-color plugin.
   *
   * @param el
   *          The element whose colour to strobe
   * @param strobeCssColor
   *          The colour to strobe to
   * @param normalCssColor
   *          The normal colour for the element (has to be specified rather than detected in case the element's colour
   *          is already in the process of animating)
   */
  private native void strobe(Element el, String strobeCssColor, String normalCssColor) /*-{
    var $ = $wnd.jQuery;
    $(el).css({
      color: $.Color(strobeCssColor)
    });
    $(el).animate({
      color: $.Color(normalCssColor)
    }, { duration: 1000, queue: false } );
  }-*/;

  /**
   * Returns the HTML div element that contains all the information about the given tick's stock. If the document
   * doesn't have a div for that stock yet, one will be created from the prototype, appended to the document, and
   * returned.
   *
   * @param tick
   *          The tick for which you want to obtain a stock box.
   * @return The div within the current document that contains all the information about the given tick's stock. It will
   *         have been freshly cloned from the prototype if necessary.
   */
  private DivElement getStockBoxDiv(Tick tick) {
    Document document = RootPanel.getBodyElement().getOwnerDocument();

    // find our stock box, creating if necessary
    DivElement stockBoxDiv = (DivElement) document.getElementById("stockbox." + tick.getSymbol());
    if (stockBoxDiv == null) {
      DivElement prototype = (DivElement) document.getElementById("prototypeStockBox");
      stockBoxDiv = (DivElement) prototype.cloneNode(true);
      stockBoxDiv.setId("stockbox." + tick.getSymbol());
      RootPanel.getBodyElement().appendChild(stockBoxDiv);
    }
    return stockBoxDiv;
  }

  /**
   * Redraws the chart using the current data that's available. You can add to that data by calling
   * {@link #getChartData(DivElement, String, Tick[])}.
   *
   * @param stockBoxDiv
   *          The div element that contains the stock information.
   * @param startTime
   *          The earliest point in time to display on the chart's x-axis
   * @param startTime
   *          The latest point in time to display on the chart's x-axis
   */
  private native void redrawChart(DivElement stockBoxDiv, double startTime, double endTime) /*-{
    var $ = $wnd.jQuery;

    var tickData = $(stockBoxDiv).data("tickData");
    var chartDiv = $(stockBoxDiv).find(".chart");
    $.plot(chartDiv, [ tickData ], { xaxis: { mode: "time", min: startTime, max: endTime } });
  }-*/;

  /**
   * Returns the JavaScript array that contains the tick data for the given div element, creating it if necessary. The
   * data array is invisible to the user, but it is used in the rendering of the tick history chart.
   *
   * @see #redrawChart(DivElement)
   *
   * @param stockBoxDiv
   *          The div that the data is attached to.
   * @return The array of chart data that gets plotted by {@link #redrawChart(DivElement)}. Each entry is an array in
   *         the form {@code [ time, price ]} (both values are JavaScript doubles). Additions to the returned array will
   *         persist, but they will only appear in the rendered chart after a call to {@link #redrawChart(DivElement)}.
   */
  private native JsArray<JsArrayNumber> getChartData(DivElement stockBoxDiv) /*-{
    var $ = $wnd.jQuery;

    // we store the tick history as data on the element for several reasons:
    // * no need to reparse JSON on every chart update
    // * keeping it in a Java Map keyed on symbol requires horrible JavaScript code for accessing it
    // * the data is removed exactly when we can't use it anymore (because the stock info div is gone)

    var tickData = $(stockBoxDiv).data("tickData");
    if (tickData == null) {
      tickData = [];
      $(stockBoxDiv).data("tickData", tickData);
    }
    return tickData;
  }-*/;

  /**
   * Adds the given tick data to the given history array.
   *
   * @param history
   *          The array to insert into. Normally this is an array obtained from
   *          {@link #getChartData(DivElement, String, Tick[])}.
   * @param tick
   *          The tick to add to the given history array.
   */
  private void addTick(JsArray<JsArrayNumber> history, Tick tick) {
    addTick(history, tick.getTime().getTime(), tick.getPrice().doubleValue(), chartTimeSpan);
  }

  /**
   * Adds the given tick data to the given history array. This is a subroutine of {@link #addTick(JsArray, Tick)}.
   *
   * @param history
   *          The array to insert into. Normally this is an array obtained from
   *          {@link #getChartData(DivElement, String, Tick[])}.
   * @param time
   *          The {@link System#currentTimeMillis()} time expressed as a double
   * @param price
   *          The stock price in dollars at the given time
   */
  private native void addTick(JsArray<JsArrayNumber> history, double time, double price, double pruneThreshold) /*-{
    history.push([time, price]);

    // prune entries too old to be visible
    var startTime = time - pruneThreshold;
    while (history[0][0] < startTime) {
      history.shift();
    }
  }-*/;
}