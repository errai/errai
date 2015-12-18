/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.framework.ChaosMonkey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class ShadowServicesTests extends AbstractErraiTest {
  private final List<Timer> timerList = new ArrayList<Timer>();
  private final List<Subscription> cleanUpPile = new ArrayList<Subscription>();

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    super.gwtTearDown();
    bus.clearProperties();
    for (Timer timer : timerList) {
      timer.cancel();
    }
    for (Subscription subscription : cleanUpPile) {
      subscription.remove();
    }
  }

  private Timer cleaned(Timer timer) {
    timerList.add(timer);
    return timer;
  }

  private Subscription cleaned(Subscription subscription) {
    cleanUpPile.add(subscription);
    return subscription;
  }

  public void testShadowServicesKickInProperty() {
    final List<String> receivedMessages = new ArrayList<String>();

    final String receivingService = "AwesomousPrime";
    final String remoteService = "MaximumAwesome";
    final List<String> expected = new ArrayList<String>();
    expected.add("FromServer");
    expected.add("FromShadow");

    cleaned(bus.subscribe(receivingService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final String value = message.getValue(String.class);
        receivedMessages.add(value);
        System.out.println("Received: " + value);
      }
    }));

    cleaned(bus.subscribeShadow(remoteService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        MessageBuilder.createConversation(message).subjectProvided().withValue("FromShadow").done().reply();
      }
    }));

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        cleaned(new Timer() {
          @Override
          public void run() {
            MessageBuilder.createMessage().toSubject(remoteService).withValue("Test!").done()
                    .repliesToSubject(receivingService).sendNowWith(bus);
          }
        }).scheduleRepeating(500);

        cleaned(new Timer() {
          @Override
          public void run() {
            if (receivedMessages.contains("FromServer")) {
              System.out.println("Received message from server. "
                      + "Now stopping bus to let shadow service kick in.");
              bus.stop(false);
            }
          }
        }).schedule(3000);

        cleaned(new Timer() {
          @Override
          public void run() {
            if (receivedMessages.containsAll(expected)) {
              finishTest();
              cancel();
            }
          }
        }).scheduleRepeating(1000);
      }
    });
  }

  public void testDeferralsGetSentToShadow() {
    final List<String> receivedMessages = new ArrayList<String>();

    final String receivingService = "AwesomousPrime";
    final String remoteService = "MaximumAwesome";

    // Tell the Chaos Monkey, who lives inside the bus, to not really connect to
    // the server
    // when asked to do so, leaving the bus in a perpetual CONNECTING state.
    bus.setProperty(ChaosMonkey.DONT_REALLY_CONNECT, "true");
    bus.setProperty(ChaosMonkey.FAIL_ON_CONNECT_AFTER_MS, "2000");

    // Normally, when the bus is explicitly stopped, the state transitions to
    // LOCAL_ONLY (which is good).
    // But for this test, we want the bus to drop back to the UNINITIALIZED
    // state to simulate a cold start.
    bus.setProperty(ChaosMonkey.DEGRADE_TO_UNINITIALIZED_ON_STOP, "true");

    bus.stop(false);

    bus.subscribe(receivingService, new MessageCallback() {
      @Override
      public void callback(final Message message) {

        final String value = message.getValue(String.class);
        receivedMessages.add(value);
        System.out.println("Received: " + value);
      }
    });

    bus.subscribeShadow(remoteService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        MessageBuilder.createConversation(message).subjectProvided().withValue("FromShadow").done().reply();
      }
    });

    bus.init();

    final Message message = MessageBuilder.createMessage().toSubject(remoteService).withValue("ZZZ").done()
            .repliesToSubject(receivingService).getMessage();

    bus.send(message);
    bus.send(message);
    bus.send(message);

    /**
     * We prove the messages are deferred.
     */
    assertEquals(0, receivedMessages.size());

    delayTestFinish(30000);

    new Timer() {
      @Override
      public void run() {

        /**
         * We prove the deferrals were delivered to the shadow service.
         */
        assertEquals(3, receivedMessages.size());
        finishTest();
      }
    }.schedule(5000);
  }
}
