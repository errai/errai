package org.jboss.errai.cdi.producer.client;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@EntryPoint
public class BeanConstrConsumersMultiProducers {
  private Event<String> testEvent;
  private RootPanel panel;
  private Label response;
  private Label greeting;
  private Label parting;

  private boolean postConstrCalled = false;

  public BeanConstrConsumersMultiProducers() {
  }

  @Inject
  public BeanConstrConsumersMultiProducers(Event<String> testEvent, RootPanel panel, @Response Label response,
                                           @Greets Label greeting, @Parts Label parting) {
    this.testEvent = testEvent;
    this.panel = panel;
    this.response = response;
    this.greeting = greeting;
    this.parting = parting;
  }

  @PostConstruct
  public void heyThere() {
    postConstrCalled = true;
  }


  @Produces
  @Response
  private Label produceResponseLabel() {
    return new Label("<No Response!!!>");
  }

  @Produces @Greets
  private static Label produceGreeting() {
    return new Label("Hello, there!!!");
  }

  @Produces @Parts
  private static Label produceParting() {
    return new Label("Goodbye, there!!!");
  }


  public Event<String> getTestEvent() {
    return testEvent;
  }

  public RootPanel getPanel() {
    return panel;
  }

  public Label getResponse() {
    return response;
  }

  public Label getGreeting() {
    return greeting;
  }

  public Label getParting() {
    return parting;
  }

  public boolean isPostConstrCalled() {
    return postConstrCalled;
  }
}
