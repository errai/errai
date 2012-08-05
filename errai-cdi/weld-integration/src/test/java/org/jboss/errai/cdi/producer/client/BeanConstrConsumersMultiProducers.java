package org.jboss.errai.cdi.producer.client;

import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@EntryPoint
public class BeanConstrConsumersMultiProducers {
  private Event<String> testEvent;
  private FooLabel response;
  private FooLabel greeting;
  private FooLabel parting;

  private boolean postConstrCalled = false;

  public BeanConstrConsumersMultiProducers() {
  }

  @Inject
  public BeanConstrConsumersMultiProducers(Event<String> testEvent, @Response FooLabel response,
                                           @Greets FooLabel greeting, @Parts FooLabel parting) {
    this.testEvent = testEvent;
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
  private FooLabel produceResponseLabel() {
    return new FooLabel("<No Response!!!>");
  }

  @Produces @Greets
  private static FooLabel produceGreeting() {
    return new FooLabel("Hello, there!!!");
  }

  @Produces @Parts
  private static FooLabel produceParting() {
    return new FooLabel("Goodbye, there!!!");
  }


  public Event<String> getTestEvent() {
    return testEvent;
  }

  public FooLabel getResponse() {
    return response;
  }

  public FooLabel getGreeting() {
    return greeting;
  }

  public FooLabel getParting() {
    return parting;
  }

  public boolean isPostConstrCalled() {
    return postConstrCalled;
  }
}
