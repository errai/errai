package org.jboss.errai.demo.busstress.client.local;

import org.jboss.errai.bus.client.tests.AbstractErraiTest;
import org.jboss.errai.ioc.client.Container;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;

public class HelloWorldClientTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.demo.busstress.App";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    new IOCBeanManagerLifecycle().resetBeanManager();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  public void testSendMessage() throws Exception {
    ErraiIocTestHelper.afterBusInitialized(new Runnable() {
      @Override
      public void run() {
        final StressTestClient client = ErraiIocTestHelper.instance.client;
        assertNotNull(client);

        // send a message using the bus (it is now initialized)
        client.messageInterval.setValue(10);
        client.messageMultiplier.setValue(1);
        client.messageSize.setValue(100);
        client.onStartButtonClick(null);

        // wait a few seconds, then check that the server response caused a DOM update
        new Timer() {
          @Override
          public void run() {
            client.stopIfRunning();
            StatsPanel statsPanel = (StatsPanel) client.resultsPanel.getWidget(0);
            assertNotNull("Stats panel should have been added to results panel", statsPanel);

            assertTrue("Expected at least one message received; got " + statsPanel.messageRecvCount,
                Integer.parseInt(statsPanel.messageRecvCount.getText()) > 0);
            finishTest();
          }
        }.schedule(2000);

      }
    });
    delayTestFinish(120000);
  }
}
