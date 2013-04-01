package org.jboss.errai.ui.test.i18n.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

public class I18nTemplateTest extends AbstractErraiCDITest {

  /**
   * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
   */
  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  /**
   * Tests that the bundle is created and is accessible.
   */
  @Test
  public void testBundleAccess() {
    I18nTemplateTestApp app = IOC.getBeanManager().lookupBean(org.jboss.errai.ui.test.i18n.client.I18nTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());
    assertEquals("Welcome to the errai-ui i18n demo.", app.getComponent().getWelcome_p().getInnerText());
    assertEquals("Label 1:", app.getComponent().getLabel1().getText());
    assertEquals("value one", app.getComponent().getVal1().getText());
    assertEquals("Label 2:", app.getComponent().getLabel2().getText());
    assertEquals("value two", app.getComponent().getVal2().getText());
  }

}