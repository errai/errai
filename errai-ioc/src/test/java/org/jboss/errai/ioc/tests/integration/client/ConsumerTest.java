package org.jboss.errai.ioc.tests.integration.client;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.Consumer;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.annotations.ReplyTo;
import org.jboss.errai.bus.client.api.annotations.ToSubject;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Timer;

public class ConsumerTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.integration.IOCIntegrationTests";
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
    Consumer<List<String>> listConsumer;
  }

  @Service
  static class ClientListService implements MessageCallback {

    static List<String> latestResponse;

    @SuppressWarnings("unchecked")
    @Override
    public void callback(Message message) {
      latestResponse = message.get(List.class, MessageParts.Value);
    }

  }

  public void testConsumerWasInjected() {
    assertNotNull(ConsumerTestInjectionPoint.instance.listConsumer);
  }

  public void testMessageRoundtrip() {
    List<String> originalList = Arrays.asList("this", "is", "my", "list");
    ClientListService.latestResponse = null;
    ConsumerTestInjectionPoint.instance.listConsumer.consume(originalList);

    new Timer() {
      @Override
      public void run() {
        if (ClientListService.latestResponse != null) {
          assertEquals(Arrays.asList("THIS", "IS", "MY", "LIST"), ClientListService.latestResponse);
        } else {
          schedule(100);
        }
      }

    }.schedule(100);
    delayTestFinish(5000);
  }

}
