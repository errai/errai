/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.util;

import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The built-in, default error dialog.
 */
public class BusErrorDialog extends DialogBox {
  boolean showErrors = !GWT.isProdMode();
  Panel contentPanel = new AbsolutePanel();

  public BusErrorDialog(final ClientMessageBusImpl clientMessageBus) {
    hide();
    setModal(false);

    final VerticalPanel panel = new VerticalPanel();

    final HorizontalPanel titleBar = new HorizontalPanel();
    titleBar.getElement().getStyle().setProperty("backgroundColor", "#A9A9A9");
    titleBar.getElement().getStyle().setWidth(100, Style.Unit.PCT);
    titleBar.getElement().getStyle().setProperty("borderBottom", "1px solid black");
    titleBar.getElement().getStyle().setProperty("marginBottom", "5px");

    final Label titleBarLabel = new Label("An Error Occurred in the Bus");
    titleBarLabel.getElement().getStyle().setFontSize(10, Style.Unit.PT);
    titleBarLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLDER);
    titleBarLabel.getElement().getStyle().setColor("white");

    titleBar.add(titleBarLabel);
    titleBar.setCellVerticalAlignment(titleBarLabel, HasVerticalAlignment.ALIGN_MIDDLE);

    final HorizontalPanel buttonPanel = new HorizontalPanel();

    final CheckBox showFurtherErrors = new CheckBox();
    showFurtherErrors.setValue(showErrors);
    showFurtherErrors.setText("Show further errors");
    showFurtherErrors.getElement().getStyle().setFontSize(10, Style.Unit.PT);
    showFurtherErrors.getElement().getStyle().setColor("white");

    showFurtherErrors.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        showErrors = booleanValueChangeEvent.getValue();
      }
    });

    final Button disconnectFromServer = new Button("Disconnect Bus");
    disconnectFromServer.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        if (Window
            .confirm("Are you sure you want to disconnect and de-federate the local bus from the server bus? "
                + "This will permanently kill your session. You will need to refresh to reconnect. OK will proceed. Click "
                + "Cancel to abort this operation")) {
          clientMessageBus.stop(true);
        }
      }
    });

    final Button clearErrors = new Button("Clear Log");
    clearErrors.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        contentPanel.clear();
      }
    });

    final Button closeButton = new Button("Dismiss Error");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        BusErrorDialog.this.hide();
      }
    });

    buttonPanel.add(showFurtherErrors);
    buttonPanel.add(disconnectFromServer);
    buttonPanel.add(clearErrors);
    buttonPanel.add(closeButton);

    buttonPanel.setCellVerticalAlignment(showFurtherErrors, HasVerticalAlignment.ALIGN_MIDDLE);

    titleBar.add(buttonPanel);
    titleBar.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);

    panel.add(titleBar);

    final Style s = panel.getElement().getStyle();

    s.setProperty("border", "1px");
    s.setProperty("borderStyle", "solid");
    s.setProperty("borderColor", "black");
    s.setProperty("backgroundColor", "#ede0c3");

    resize();

    panel.add(contentPanel);
    add(panel);

    getElement().getStyle().setZIndex(16777271);   // WTF? 2^24 + 55?
  }

  public void addError(final String message, final String additionalDetails, final Throwable e) {
    if (!showErrors)
      return;

    contentPanel.add(new HTML("<strong style='background:red;color:white;'>" + message + "</strong>"));

    final StringBuilder buildTrace = new StringBuilder("<tt style=\"font-size:11px;\"><pre>");
    if (e != null) {
      e.printStackTrace();
      buildTrace.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("<br/>");
      for (final StackTraceElement ste : e.getStackTrace()) {
        buildTrace.append("  ").append(ste.toString()).append("<br/>");
      }
    }
    buildTrace.append("</pre>");

    contentPanel.add(new HTML(buildTrace.toString() + "<br/><strong>Additional Details:</strong>" + additionalDetails
        + "</tt>"));

    if (!isShowing()) {
      resize();
      show();
      center();
    }
  }

  private void resize() {
    contentPanel.setWidth(Window.getClientWidth() * 0.90 + "px");
    contentPanel.setHeight(Window.getClientHeight() * 0.90 + "px");
    contentPanel.getElement().getStyle().setProperty("overflow", "auto");
  }
}
