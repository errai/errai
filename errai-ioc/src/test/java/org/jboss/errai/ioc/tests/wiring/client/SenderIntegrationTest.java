package org.jboss.errai.ioc.tests.wiring.client;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.tests.AbstractErraiTest;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.api.ReplyTo;
import org.jboss.errai.ioc.client.api.Sender;
import org.jboss.errai.ioc.client.api.ToSubject;

import com.google.gwt.user.client.Timer;

public class SenderIntegrationTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  @Singleton
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
  
  @Service
  public static class TestCompleterService implements MessageCallback {
    static boolean replyReceived = false;
    
    @Override
    public void callback(Message message) {
      replyReceived = true;
    }
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
    new Container().onModuleLoad();
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

}
