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

package org.jboss.errai.bus.client.tests;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

import com.google.gwt.user.client.Timer;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class MessageReplyCallbackTest extends AbstractErraiTest {

  private static final int TIMEOUT = 60000;
  private static final int POLL = 500;

  private MessageBus bus;
  private boolean received;
  private MessageCallback callback = new MessageCallback() {
    @Override
    public void callback(Message message) {
      received = true;
    }
  };

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.MessageReplyCallbackTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    bus = ErraiBus.get();
    received = false;
  }

  public void testSendViaDefaultMessageBuilder() throws Exception {
    runAndWait(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage().toSubject("ReplyCallbackTestService").done().repliesTo(callback)
                .sendNowWith(bus);
      }
    });
  }

  public void testSendViaMessage() throws Exception {
    runAndWait(new Runnable() {
      @Override
      public void run() {
        Message message = MessageBuilder.createMessage().toSubject("ReplyCallbackTestService").done()
                .repliesTo(callback).getMessage();
        message.sendNowWith(bus);
      }
    });
  }

  public void testReuseMessage() {
    final Message message = MessageBuilder.createMessage("ReplyCallbackTestService").done().repliesTo(callback)
            .getMessage();
    runAndWaitAndThen(new Runnable() {
      @Override
      public void run() {
        message.sendNowWith(bus);
      }
    }, new Runnable() {
      @Override
      public void run() {
        received = false;
        message.sendNowWith(bus);
      }

    });
  }

  /*
   * This test in a nut shell: 
   *    - send message to ReplyCallbackTestService and wait for first reply 
   *    - send second message to ReplyCallbackTestService by building conversation
   *    - test that this conversation was repliable by waiting for second reply from ReplyCallbackTestService
   */
  public void testReplyToConversationViaMessage() {
    delayTestFinish(TIMEOUT);
    MessageBuilder.createMessage("ReplyCallbackTestService").done().repliesTo(new MessageCallback() {
      @Override
      public void callback(Message message) {
        final Message m = message;
        runAndWait(new Runnable() {
          @Override
          public void run() {
            MessageBuilder.createConversation(m).subjectProvided().done().repliesTo(callback).getMessage()
                    .sendNowWith(bus);
          }
        });
      }
    }).sendNowWith(bus);
  }
  
  /*
   * This test in a nut shell: 
   *    - send message to ReplyCallbackTestService and wait for first reply 
   *    - send second message to ReplyCallbackTestService by building conversation
   *    - test that this conversation was repliable by waiting for second reply from ReplyCallbackTestService
   */
  public void testReplyToConversationViaDefaultBuilder() {
    delayTestFinish(TIMEOUT);
    MessageBuilder.createMessage("ReplyCallbackTestService").done().repliesTo(new MessageCallback() {
      @Override
      public void callback(Message message) {
        final Message m = message;
        runAndWait(new Runnable() {
          @Override
          public void run() {
            MessageBuilder.createConversation(m).subjectProvided().done().repliesTo(callback).sendNowWith(bus);
          }
        });
      }
    }).sendNowWith(bus);
  }
  
  public void testSendGlobalViaDefaultBuilder() {
    runAndWait(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("ReplyCallbackTestService").done().repliesTo(callback).sendGlobalWith(bus);
      }
    });
  }
  
  public void testSendNoListenersViaDefaultBuilder() {
    runAndWait(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("ReplyCallbackTestService").done().repliesTo(callback).sendNowWith(bus, false);
      }
    });
  }

  private void runAndWait(Runnable test) {
    runAndWaitAndThen(test, null);
  }

  private void runAndWaitAndThen(Runnable first, final Runnable second) {
    delayTestFinish(TIMEOUT + 2 * POLL);
    final long start = System.currentTimeMillis();
    first.run();
    new Timer() {
      @Override
      public void run() {
        if (System.currentTimeMillis() - start > TIMEOUT) {
          cancel();
          fail();
        }
        else if (received) {
          cancel();
          if (second != null) {
            runAndWaitAndThen(second, null);
          }
          else {
            finishTest();
          }
        }
      }
    }.scheduleRepeating(POLL);
  }
}
