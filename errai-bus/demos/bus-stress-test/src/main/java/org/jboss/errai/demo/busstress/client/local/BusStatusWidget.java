package org.jboss.errai.demo.busstress.client.local;

import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

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

  private Resources resources = GWT.create(Resources.class);

  Image statusImage = new Image(resources.white());

  public BusStatusWidget() {
    initWidget(statusImage);
  }

  @Override
  public void busAssociating(BusLifecycleEvent e) {
    statusImage.setResource(resources.yellow());
  }

  @Override
  public void busDisassociating(BusLifecycleEvent e) {
    statusImage.setResource(resources.red());
  }

  @Override
  public void busOnline(BusLifecycleEvent e) {
    statusImage.setResource(resources.green());
  }

  @Override
  public void busOffline(BusLifecycleEvent e) {
    statusImage.setResource(resources.yellow());
  }

}
