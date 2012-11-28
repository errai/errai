package org.jboss.errai.demo.busstress.client.local;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class BusStatusWidget extends Composite implements BusLifecycleListener {

  interface Resources extends ClientBundle {
    @Source("white.png")
    ImageResource white();

    @Source("red.png")
    ImageResource red();

    @Source("yellow.png")
    ImageResource yellow();

    @Source("green.png")
    ImageResource green();
  }

  private final HorizontalPanel me = new HorizontalPanel();
  private final Resources resources = GWT.create(Resources.class);
  private final Image statusImage = new Image(resources.white());
  private final Label statusLabel = new Label("Unknown");
  private final Button stopTrueButton = new Button("bus.stop(true)");
  private final Button stopFalseButton = new Button("bus.stop(false)");
  private final Button initButton = new Button("bus.init()");
  private final CheckBox busInitialized = new CheckBox("bus.initialized");

  public BusStatusWidget() {
    initWidget(me);

    statusLabel.setWordWrap(false);
    statusLabel.setWidth("6em");

    stopTrueButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ((ClientMessageBusImpl) ErraiBus.get()).stop(true);
      }
    });

    stopFalseButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ((ClientMessageBusImpl) ErraiBus.get()).stop(false);
      }
    });

    initButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ((ClientMessageBus) ErraiBus.get()).init();
      }
    });

    busInitialized.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ((ClientMessageBusImpl) ErraiBus.get()).setInitialized(busInitialized.getValue());
      }
    });

    me.add(statusImage);
    me.add(statusLabel);
    me.add(stopFalseButton);
    me.add(stopTrueButton);
    me.add(initButton);
    me.add(busInitialized);
  }

  @Override
  public void busAssociating(BusLifecycleEvent e) {
    statusLabel.setText("Connecting");
    statusImage.setResource(resources.yellow());
    busInitialized.setValue(e.getBus().isInitialized());
  }

  @Override
  public void busDisassociating(BusLifecycleEvent e) {
    statusLabel.setText("Local Only");
    statusImage.setResource(resources.red());
    busInitialized.setValue(e.getBus().isInitialized());
  }

  @Override
  public void busOnline(BusLifecycleEvent e) {
    statusLabel.setText("Connected");
    statusImage.setResource(resources.green());
    busInitialized.setValue(e.getBus().isInitialized());
  }

  @Override
  public void busOffline(BusLifecycleEvent e) {
    statusLabel.setText("Connecting");
    statusImage.setResource(resources.yellow());
    busInitialized.setValue(e.getBus().isInitialized());
  }

}
