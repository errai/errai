package org.jboss.errai.cdi.demo.mvp.client.local;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Main application entry point.
 */
@EntryPoint
public class Contacts {

  private HandlerManager eventBus = new HandlerManager(null);

  @Inject
  private AppController appController;
 
  @AfterInitialization
  public void startApp() {
    appController.go(RootPanel.get());
  }

  @Produces
  private HandlerManager produceEventBus() {
    return eventBus;
  }
}