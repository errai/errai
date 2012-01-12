package org.jboss.errai.ioc.tests.wiring.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.ioc.client.api.Sender;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.annotations.ReplyTo;
import org.jboss.errai.bus.client.api.annotations.ToSubject;
import org.jboss.errai.bus.client.tests.AbstractErraiTest;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class ConsumerTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  @EntryPoint
  public static class ConsumerTestInjectionPoint {
    static ConsumerTestInjectionPoint instance;

    public ConsumerTestInjectionPoint() {
      instance = this;
    }

    @Inject
    @ToSubject("ListCapitializationService")
    @ReplyTo("ClientListService")
    Sender<List<String>> listSender;
  }

  @Service
  public static class ClientListService implements MessageCallback {

    static List<String> latestResponse;

    @SuppressWarnings("unchecked")
    @Override
    public void callback(Message message) {
      latestResponse = message.get(List.class, MessageParts.Value);
    }

  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
    new Container().onModuleLoad();
  }

  public void testConsumerWasInjected() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        new Timer() {
          @Override
          public void run() {
            assertNotNull(ConsumerTestInjectionPoint.instance.listSender);
            finishTest();
          }
        }.schedule(1000);


      }
    });
  }

  public void testMessageRoundtrip() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        List<String> originalList = Arrays.asList("this", "is", "my", "list");
        ClientListService.latestResponse = null;
        ConsumerTestInjectionPoint.instance.listSender.consume(originalList);

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

}
