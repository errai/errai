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

package org.jboss.errai.ioc.support.bus.tests.client;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.ReplyTo;
import org.jboss.errai.ioc.client.api.ToSubject;
import org.jboss.errai.ioc.support.bus.client.Sender;

import com.google.gwt.user.client.Timer;

public class SenderIntegrationTest extends AbstractErraiIOCBusTest {
  @EntryPoint
  public static class SenderTestInjectionPoint {
    static SenderTestInjectionPoint instance;

    public SenderTestInjectionPoint() {
      instance = this;
    }

    @Inject
    @ToSubject("ListCapitializationService")
    @ReplyTo("ClientListService")
    Sender<List<String>> replySender;

    @Inject
    @ToSubject("EmptyReplyService")
    Sender<List<String>> noReplySender;

    @Inject
    @ToSubject("NonExistingService")
    Sender<List<String>> brokenSender;
  }

  @EntryPoint
  @Service
  public static class ClientListService implements MessageCallback {
    static List<String> latestResponse;

    @SuppressWarnings("unchecked")
    @Override
    public void callback(Message message) {
      latestResponse = message.get(List.class, MessageParts.Value);
    }
  }

  @EntryPoint
  @Service
  public static class TestCompleterService implements MessageCallback {
    static boolean replyReceived = false;

    @Override
    public void callback(Message message) {
      replyReceived = true;
    }
  }

  public void testSenderWasInjected() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        new Timer() {
          @Override
          public void run() {
            assertNotNull(SenderTestInjectionPoint.instance.replySender);
            finishTest();
          }
        }.schedule(1000);
      }
    });
  }

  public void testSenderWithReplyTo() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        List<String> originalList = Arrays.asList("this", "is", "my", "list");
        ClientListService.latestResponse = null;
        SenderTestInjectionPoint.instance.replySender.send(originalList);

        new Timer() {
          @Override
          public void run() {
            if (ClientListService.latestResponse != null) {
              assertEquals(Arrays.asList("THIS", "IS", "MY", "LIST"), ClientListService.latestResponse);
              finishTest();
            }
            else {
              schedule(100);
            }
          }

        }.schedule(100);
      }
    });
  }

  public void testSenderWithoutReplyTo() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        List<String> originalList = Arrays.asList("this", "is", "my", "list");
        TestCompleterService.replyReceived = false;
        SenderTestInjectionPoint.instance.noReplySender.send(originalList);

        new Timer() {
          @Override
          public void run() {
            if (TestCompleterService.replyReceived) {
              finishTest();
            }
            else {
              schedule(100);
            }
          }

        }.schedule(100);
      }
    });
  }

  public void testSenderWithErrorCallback() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        List<String> originalList = Arrays.asList("this", "is", "my", "list");
        ClientListService.latestResponse = null;
        SenderTestInjectionPoint.instance.brokenSender.send(originalList,
            new ErrorCallback<Message>() {
              @Override
              public boolean error(Message message, Throwable throwable) {
                assertNotNull("Throwable is null.", throwable);
                try {
                  throw throwable;
                }
                catch(NoSubscribersToDeliverTo e) {
                  finishTest();
                }
                catch (Throwable t) {
                  fail("Received wrong Throwable!");
                }
                return false;
              }
            });
      }
    });
  }
}
