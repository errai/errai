package org.jboss.errai.cdi.event.client.test;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.cdi.event.client.ClientLocalEventTestModule;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
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

}
