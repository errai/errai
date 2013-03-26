package org.jboss.errai.ui.test.i18n.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.shared.DictionaryRegistry;
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
  public void testBundleAccess() throws Exception {
    try {
      I18nTemplateTestApp app = IOC.getBeanManager().lookupBean(org.jboss.errai.ui.test.i18n.client.I18nTemplateTestApp.class).getInstance();
      assertNotNull(app.getComponent());
      DictionaryRegistry dictionaryRegistry = IOC.getBeanManager().lookupBean(DictionaryRegistry.class).getInstance();
      assertNotNull(dictionaryRegistry);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

}