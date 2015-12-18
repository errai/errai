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

package org.jboss.errai.cdi.event.client.test;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.Container;

import com.google.gwt.user.client.Timer;

public class ServerLocalEventIntegrationTest extends AbstractErraiCDITest {

  private MessageBus bus = ErraiBus.get();
  public static final String SUCCESS = "SUCCESS";
  public static final String FAILURE = "FAILURE";
  private final int TIMEOUT = 30000;
  
  public ServerLocalEventIntegrationTest() {
    InitVotes.registerOneTimePreInitCallback(new Runnable() {
      @Override
      public void run() {
        setup();
      }
    });
  }
  
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.LocalEventTestModule";
  }
  
  private void setup() {
    bus.unsubscribeAll(SUCCESS);
    bus.unsubscribeAll(FAILURE);
    bus.subscribe(SUCCESS, new MessageCallback() {
      @Override
      public void callback(Message message) {
        finishTest();
      }
    });
    bus.subscribe(FAILURE, new MessageCallback() {
      @Override
      public void callback(Message message) {
        fail();
      }
    });
  }
  
  public void testServerReceivesLocalEvent() throws Exception {
    delayTestFinish(TIMEOUT);
    MessageBuilder.createMessage("fireEventB").signalling().noErrorHandling().sendNowWith(bus);
  }
  
  public void testClientDoesNotReceiveLocalEvent() throws Exception {
    delayTestFinish(TIMEOUT);
    final long start = System.currentTimeMillis();
    Container.$(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("fireEventA").signalling().noErrorHandling().sendNowWith(bus);
        new Timer() {
          @Override
          public void run() {
            if (System.currentTimeMillis() - start > TIMEOUT - 500) {
              cancel();
              finishTest();
            }
          }
        }.scheduleRepeating(200);
      }
    });
  }
}
