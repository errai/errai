package org.jboss.errai.bus.client.tests;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;

/**
 * @author Mike Brock
 */
public class BusRenegotiationTests extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  public void testBusRecoversFromSessionExpiry() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage()
            .toSubject("ExpiryService")
            .signalling().noErrorHandling().sendNowWith(ErraiBus.get());

        new Timer() {
          public void run() {
            MessageBuilder.createMessage()
                .toSubject("TestService3")
                .signalling().noErrorHandling().repliesTo(
                new MessageCallback() {
                  @Override
                  public void callback(Message message) {
                    finishTest();

                  }
                }
            ).sendNowWith(ErraiBus.get());
          }
        }.schedule(500);
      }
    });

  }
}
