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

import java.util.List;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.cdi.event.client.ClientLocalEventAObserver;
import org.jboss.errai.cdi.event.client.ClientLocalEventTestModule;
import org.jboss.errai.cdi.event.client.shared.JsTypeEvent;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.JsTypeEventObserver;
import org.jboss.errai.enterprise.client.cdi.WindowEventObservers;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.user.client.Timer;

public class ClientLocalEventIntegrationTest extends AbstractErraiCDITest {

  MessageBus bus = ErraiBus.get();
  private final int TIMEOUT = 30000;

  public static final String SUCCESS = "SUCCESS";
  public static final String FAILURE = "FAILURE";

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.LocalEventTestModule";
  }

  public ClientLocalEventIntegrationTest() {
    InitVotes.registerOneTimePreInitCallback(new Runnable() {
      @Override
      public void run() {
        setup();
      }
    });
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

  public void testLocalEventSentToClient() throws Exception {
    delayTestFinish(TIMEOUT);
    /*
     * This must be run in a Timer to ensure the method exits (so a call to finishTest does not
     * throw an IllegalStateException) and is run using Container.$ to ensure that it is run after
     * Bootstrapping.
     */
    new Timer() {
      @Override
      public void run() {
        Container.$(new Runnable() {
          @Override
          public void run() {
            IOC.getBeanManager().lookupBean(ClientLocalEventTestModule.class).getInstance().fireEventA();
          }
        });
      }
    }.schedule(500);
  }

  public void testLocalEventNotReceivedByServer() throws Exception {
    delayTestFinish(TIMEOUT);
    final long start = System.currentTimeMillis();
    Container.$(new Runnable() {
      @Override
      public void run() {
        IOC.getBeanManager().lookupBean(ClientLocalEventTestModule.class).getInstance().fireEventB();
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
  
  public void testJsTypeEventObservable() throws Exception {
    delayTestFinish(TIMEOUT);
    new Timer() {
      @Override
      public void run() {
        Container.$(new Runnable() {
          @Override
          public void run() {
            IOC.getBeanManager().lookupBean(ClientLocalEventTestModule.class).getInstance().fireJsTypeEvent();
          }
        });
      }
    }.schedule(500);
    
    new Timer() {
      @Override
      public void run() {
        final List<JsTypeEventObserver<?>> jsTypeObservers = WindowEventObservers.createOrGet().get(JsTypeEvent.class.getName());
        
        assertFalse(jsTypeObservers.isEmpty());
        assertTrue(IOC.getBeanManager().lookupBean(ClientLocalEventAObserver.class).getInstance().isJsTypeEventObserved());
        finishTest();
      }
    }.schedule(3000);
  }

}
