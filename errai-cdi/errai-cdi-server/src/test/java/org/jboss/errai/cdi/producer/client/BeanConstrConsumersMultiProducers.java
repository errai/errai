/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.producer.client;

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
  @ApplicationScoped
  private FooLabel produceResponseLabel() {
    return new FooLabel("<No Response!!!>");
  }

  @Produces @Greets @ApplicationScoped
  private static FooLabel produceGreeting() {
    return new FooLabel("Hello, there!!!");
  }

  @Produces @Parts @ApplicationScoped
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
