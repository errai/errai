package org.jboss.errai.bus.client.tests;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.AsyncRPCService;
import org.jboss.errai.common.client.api.RemoteCallback;



/**
 * @author Mike Brock
 */
public class AsyncRPCServicesTest extends AbstractErraiTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  public void testAsyncRPCCall() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals("foobar", response);
            finishTest();
          }
        }, AsyncRPCService.class).doSomeTask();
      }
    });
  }
}
