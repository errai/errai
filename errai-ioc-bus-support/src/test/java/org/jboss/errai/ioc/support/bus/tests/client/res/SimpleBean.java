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

package org.jboss.errai.ioc.support.bus.tests.client.res;

import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.inject.Inject;

@EntryPoint
public class SimpleBean extends SimpleSuperBean {

  @Inject
  public MessageBus bus;

  @Inject
  private RequestDispatcher dispatcher2;

  @Inject
  private MessageBus bus2;

  private RequestDispatcher dispatcher3;
  private MessageBus bus3;

  private RequestDispatcher dispatcher4;
  private MessageBus bus4;

  @Inject
  private ClientMessageBus clientMessageBus;



  @Inject
  public SimpleBean(RequestDispatcher dispatcher3, MessageBus bus3) {
    this.dispatcher3 = dispatcher3;
    this.bus3 = bus3;
  }

  public RequestDispatcher getDispatcher() {
    return dispatcher;
  }

  public void setDispatcher(RequestDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public MessageBus getBus() {
    return bus;
  }

  public void setBus(MessageBus bus) {
    this.bus = bus;
  }

  public RequestDispatcher getDispatcher2() {
    return dispatcher2;
  }

  public void setDispatcher2(RequestDispatcher dispatcher2) {
    this.dispatcher2 = dispatcher2;
  }

  public MessageBus getBus2() {
    return bus2;
  }

  public void setBus2(MessageBus bus2) {
    this.bus2 = bus2;
  }

  public RequestDispatcher getDispatcher3() {
    return dispatcher3;
  }

  public void setDispatcher3(RequestDispatcher dispatcher3) {
    this.dispatcher3 = dispatcher3;
  }

  public MessageBus getBus3() {
    return bus3;
  }

  public void setBus3(MessageBus bus3) {
    this.bus3 = bus3;
  }


  public RequestDispatcher getDispatcher4() {
    return dispatcher4;
  }

  @Inject
  public void setDispatcher4(RequestDispatcher dispatcher4) {
    this.dispatcher4 = dispatcher4;
  }

  public MessageBus getBus4() {
    return bus4;
  }

  @Inject
  public void setBus4(MessageBus bus4) {
    this.bus4 = bus4;
  }

  public ClientMessageBus getClientMessageBus() {
    return clientMessageBus;
  }
}
