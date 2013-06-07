package org.jboss.errai.bus.client.tests;



/**
 * @author Mike Brock
 */
public class AsyncRPCServicesTest extends AbstractErraiTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  public void testAsyncRPCCall() {
    System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nWARNING: this test is disabled!\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
//    delayTestFinish(30000);
//
//    runAfterInit(new Runnable() {
//      @Override
//      public void run() {
//        MessageBuilder.createCall(new RemoteCallback<String>() {
//          @Override
//          public void callback(String response) {
//            assertEquals("foobar", response);
//            finishTest();
//          }
//        }, AsyncRPCService.class).doSomeTask();
//      }
//    });
  }
}
