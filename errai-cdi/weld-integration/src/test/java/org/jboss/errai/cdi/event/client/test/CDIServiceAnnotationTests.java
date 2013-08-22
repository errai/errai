package org.jboss.errai.cdi.event.client.test;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.tests.AbstractErraiTest;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gwt.user.client.Timer;

public class CDIServiceAnnotationTests extends AbstractErraiTest {
  
  MessageBus bus = ErraiBus.get();
  private boolean received;
  public final static String REPLY_TO = "AnnotationTester";
  
  private final int POLL = 100;
  private final int TIMEOUT = 10000;

  public CDIServiceAnnotationTests() {
    super();
    bus.subscribe(REPLY_TO, new MessageCallback() {
      @Override
      public void callback(Message message) {
        received = true;
      }
    });
  }
  
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.ServiceAnnotationTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    received = false;
  }
  
  public void testClassWithServiceMethod() throws Exception {
    runServiceTest("serviceMethod", null);
  }
  
  public void testClassWithService() throws Exception {
    runServiceTest("ClassWithService", null);
  }
  
  public void testClassWithMultipleServices() throws Exception {
    runServiceTestAndThen("service1", null, new Runnable() {
      @Override
      public void run() {
        runServiceTest("service2", null);
      }
    });
  }
  
  private void runServiceTest(final String subject, String command) {
    runServiceTestAndThen(subject, command, new Runnable() {
      @Override
      public void run() {
        finishTest();
      }
    });
  }
  
  private void runServiceTestAndThen(final String subject, String command, final Runnable finish) {
    delayTestFinish(TIMEOUT + 2 * POLL);
    final long start = System.currentTimeMillis();

    (command == null ? MessageBuilder.createMessage(subject) : MessageBuilder.createMessage(subject).command(command))
            .with(MessageParts.ReplyTo, REPLY_TO).errorsHandledBy(new ErrorCallback<Message>() {
              @Override
              public boolean error(Message message, Throwable throwable) {
                throw new RuntimeException("Message could not be delivered", throwable);
              }
            }).sendGlobalWith(bus);

    new Timer() {

      @Override
      public void run() {
        if (System.currentTimeMillis() - start > TIMEOUT && !received) {
          cancel();
          fail("No response received after " + (System.currentTimeMillis() - start) + " ms");
        }
        else if (received) {
          cancel();
          finish.run();
        }
      }
    }.scheduleRepeating(POLL);
  }

}
