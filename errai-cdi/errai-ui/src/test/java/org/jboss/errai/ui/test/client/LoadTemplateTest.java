package org.jboss.errai.ui.test.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.junit.Test;

public class LoadTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.test.Test";
  }

  @Test
  public void testInitialSetup() {
    CDITestHelper.afterCdiInitialized(new Runnable() {
      @Override
      public void run() {
        App app = CDITestHelper.instance.app;
        assertNotNull(app.getTemplate());
        assertEquals("HI TEMPLATE", app.getTemplate().getText());
        finishTest();
      }
    });

    // This call tells GWT's test runner to wait 20 seconds after the test
    // returns.
    // We need this delay to give the HelloMessage time to come back from the
    // server.
    delayTestFinish(20000);
  }

}