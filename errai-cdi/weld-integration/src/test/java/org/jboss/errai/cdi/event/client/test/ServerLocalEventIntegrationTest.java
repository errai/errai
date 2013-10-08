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
