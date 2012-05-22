package org.jboss.errai.ui.test.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.junit.Test;

import com.google.gwt.user.client.ui.RootPanel;

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
        assertNotNull(app.getComponent());
        assertTrue(app.getComponent().getElement().getInnerHTML().contains("HI TEMPLATE"));

        RootPanel lbl = RootPanel.get("lbl");

        assertNotNull(lbl);

        String innerText = lbl.asWidget().getElement().getInnerText();
        assertEquals(innerText, "Added by component");
        
        finishTest();
      }
    });

    // This call tells GWT's test runner to wait 20 seconds after the test
    // returns.
    // We need this delay to give the HelloMessage time to come back from the
    // server.
    delayTestFinish(40000);
  }

}