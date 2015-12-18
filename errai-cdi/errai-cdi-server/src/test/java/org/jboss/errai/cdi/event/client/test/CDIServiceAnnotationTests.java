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
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.tests.AbstractErraiTest;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.protocols.MessageParts;

import com.google.gwt.user.client.Timer;

/**
 * Test that annotated services (types or methods) are properly scanned and subscribed by
 * CDIExtensionPoints.
 * 
 * @author mbarkley <mbarkley@redhat.com>
 */
public class CDIServiceAnnotationTests extends AbstractErraiTest {

  MessageBus bus = ErraiBus.get();
  private boolean received;
  private Message receivedMessage;
  private Timer timer;
  public final static String REPLY_TO_BASE = "AnnotationTester";
  public static String REPLY_TO;
  private static Integer counter = 0;

  private final int POLL = 100;
  private final int TIMEOUT = 10000;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.ServiceAnnotationTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    // Do this to enhance independence of tests
    REPLY_TO = REPLY_TO_BASE + ++counter;

    bus.subscribe(REPLY_TO, new MessageCallback() {
      @Override
      public void callback(Message message) {
        received = true;
        receivedMessage = message;
      }
    });
  }

  @Override
  protected void gwtTearDown() throws Exception {
    timer.cancel();
    super.gwtTearDown();
    
    bus.unsubscribeAll(REPLY_TO);
    received = false;
    receivedMessage = null;
  }

  public void testClassWithServiceMethod() throws Exception {
    runServiceTest("serviceMethod", null, null);
  }

  public void testClassWithService() throws Exception {
    runServiceTest("ClassWithService", null, null);
  }

  public void testClassWithMultipleServices() throws Exception {
    runServiceTestAndThen("service1", null, null, new Runnable() {
      @Override
      public void run() {
        runServiceTest("service2", null, null);
      }
    });
  }

  public void testClassWithCommandMethod() throws Exception {
    runServiceTest("ClassWithCommandMethod", "command", null);
  }

  public void testNamedClassWithService() throws Exception {
    runServiceTest("ANamedClassService", null, null);
  }

  public void testClassWithNamedServiceMethod() throws Exception {
    runServiceTest("ANamedServiceMethod", null, null);
  }

  public void testClassWithNamedCommandMethod() throws Exception {
    runServiceTest("ClassWithNamedCommandMethod", "ANamedCommandMethod", null);
  }

  public void testClassWithServiceAndCommandMethod() throws Exception {
    runServiceTest("ClassWithServiceAndCommandMethod", "serviceAndCommandMethod", null);
  }

  /**
   * Test that type service works with inner method service.
   */
  public void testClassWithServiceAndMethodWithService1() throws Exception {
    runServiceTest("ClassWithServiceAndMethodWithService", null, null);
  }
  
  /**
   * Test that method service works with enclosing type service.
   */
  public void testClassWithServiceAndMethodWithService2() throws Exception {
    runServiceTest("methodWithService", null, null);
  }

  /**
   * Check that a method with a service and command annotation works if it is enclosed in a service
   * type.
   */
  public void testClassWithServiceAndMethodWithServiceAndCommand1() throws Exception {
    runServiceTest("TheMethodsService", "command", null);
  }

  /**
   * Check that a type service will ignores @Command method annotations if that method also is a
   * service.
   */
  public void testClassWithServiceAndMethodWithServiceAndCommand2() throws Exception {
    runServiceTestAndThen("ClassWithServiceAndMethodWithServiceAndCommand", "command", null, new Runnable() {

      @Override
      public void run() {
        if ("callback".equals(receivedMessage.getValue(String.class))) {
          finishTest();
        }
        else {
          fail("The callback should have received this message");
        }
      }
    });
  }

  /**
   * Test that local class service does not receive message from client.
   */
  public void testClassWithLocalService1() throws Exception {
    runNonRespondingServiceTest("ClassWithLocalService");
  }

  /**
   * Test that local class service does receive message relayed through server.
   */
  public void testClassWithLocalService2() throws Exception {
    runServiceTest("LocalCDIAnnotationRouterService", null, "ClassWithLocalService");
  }

  /**
   * Test that local method service does not receive message from client.
   */
  public void testMethodWithLocalService1() throws Exception {
    runNonRespondingServiceTest("localMethodService");
  }

  /**
   * Test that local method service does receive message relayed through server.
   */
  public void testMethodWithLocalService2() throws Exception {
    runServiceTest("LocalCDIAnnotationRouterService", null, "localMethodService");
  }

  private void runNonRespondingServiceTest(String subject) {
    delayTestFinish(TIMEOUT + 2 * POLL);
    final long start = System.currentTimeMillis();

    MessageBuilder.createMessage(subject).signalling().with(MessageParts.ReplyTo, REPLY_TO)
            .errorsHandledBy(new ErrorCallback<Message>() {
              @Override
              public boolean error(Message message, Throwable throwable) {
                throw new RuntimeException("error occurred with message: " + throwable.getMessage(), throwable);
              }
            }).sendNowWith(bus);

    timer = new Timer() {

      @Override
      public void run() {
        if (System.currentTimeMillis() - start > TIMEOUT && !received) {
          cancel();
          finishTest();
        }
        else if (received) {
          cancel();
          System.out.println(receivedMessage);
          System.out.println(counter);
          fail("Message should not have been received!");
        }
      }
    };
    timer.scheduleRepeating(POLL);
  }

  private void runServiceTest(final String subject, String command, String value) {
    runServiceTestAndThen(subject, command, value, new Runnable() {
      @Override
      public void run() {
        finishTest();
      }
    });
  }

  private void runServiceTestAndThen(final String subject, String command, String value, final Runnable finish) {
    delayTestFinish(TIMEOUT + 2 * POLL);
    final long start = System.currentTimeMillis();

    MessageBuildParms<MessageBuildSendableWithReply> message;

    if (command != null) {
      message = MessageBuilder.createMessage(subject).command(command);
    }
    else
      message = MessageBuilder.createMessage(subject).signalling();

    if (value != null)
      message = message.withValue(value);

    message.with(MessageParts.ReplyTo, REPLY_TO).errorsHandledBy(new ErrorCallback<Message>() {
      @Override
      public boolean error(Message message, Throwable throwable) {
        throw new RuntimeException("error occurred with message: " + throwable.getMessage(), throwable);
      }
    }).sendNowWith(bus);

    timer = new Timer() {

      @Override
      public void run() {
        if (System.currentTimeMillis() - start > TIMEOUT && !received) {
          cancel();
          fail("No response received after " + (System.currentTimeMillis() - start) + " ms");
        }
        else if (received) {
          cancel();
          System.out.println(receivedMessage);
          System.out.println(counter);
          finish.run();
        }
      }
    };
    timer.scheduleRepeating(POLL);
  }

}
