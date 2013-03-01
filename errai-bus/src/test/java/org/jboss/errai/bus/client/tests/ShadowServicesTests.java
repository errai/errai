/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.tests;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class ShadowServicesTests extends AbstractErraiTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  public void testShadowServicesKickInProperty() {
    final List<String> receivedMessages = new ArrayList<String>();

    final String receivingService = "AwesomousPrime";
    final String remoteService = "MaximumAwesome";

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
        MessageBuilder.createConversation(message)
            .subjectProvided()
            .withValue("FromShadow")
            .done().reply();
      }
    });

    runAfterInit(new Runnable() {
      @Override
      public void run() {

        new Timer() {
          @Override
          public void run() {
            MessageBuilder.createMessage()
                .toSubject(remoteService)
                .withValue("Boop!")
                .done().repliesToSubject(receivingService).sendNowWith(bus);
          }
        }.scheduleRepeating(500);

        new Timer() {
          @Override
          public void run() {
            bus.stop(false);
          }
        }.schedule(1500);

        final List<String> expected = new ArrayList<String>();
        expected.add("FromServer");
        expected.add("FromShadow");

        new Timer() {
          @Override
          public void run() {
            if (receivedMessages.containsAll(expected)) {
              finishTest();
            }
          }
        }.scheduleRepeating(1000);
      }
    });
  }
}

