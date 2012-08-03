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
  private TestLabel response;
  private TestLabel greeting;
  private TestLabel parting;

  private boolean postConstrCalled = false;

  public BeanConstrConsumersMultiProducers() {
  }

  @Inject
  public BeanConstrConsumersMultiProducers(Event<String> testEvent, @Response TestLabel response,
                                           @Greets TestLabel greeting, @Parts TestLabel parting) {
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
  private TestLabel produceResponseLabel() {
    return new TestLabel("<No Response!!!>");
  }

  @Produces @Greets
  private static TestLabel produceGreeting() {
    return new TestLabel("Hello, there!!!");
  }

  @Produces @Parts
  private static TestLabel produceParting() {
    return new TestLabel("Goodbye, there!!!");
  }


  public Event<String> getTestEvent() {
    return testEvent;
  }

  public TestLabel getResponse() {
    return response;
  }

  public TestLabel getGreeting() {
    return greeting;
  }

  public TestLabel getParting() {
    return parting;
  }

  public boolean isPostConstrCalled() {
    return postConstrCalled;
  }
}
