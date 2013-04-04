package org.jboss.errai.ui.test.i18n.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Test;

public class I18nTemplateTest_fr_FR extends AbstractErraiCDITest {

  /**
   * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
   */
  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  /**
   * @see org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest#gwtSetUp()
   */
  @Override
  protected void gwtSetUp() throws Exception {
    TranslationService.setCurrentLocale("fr_fr");
    super.gwtSetUp();
  }

  /**
   * Tests that the bundle is created and is accessible.
   */
  @Test
  public void testBundleAccess() {
    I18nTemplateTestApp app = IOC.getBeanManager().lookupBean(org.jboss.errai.ui.test.i18n.client.I18nTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());
    assertEquals("Bienvenue sur la démo Errai-ui i18n.", app.getComponent().getWelcome_p().getInnerText());
    assertEquals("Etiquette 1:", app.getComponent().getLabel1().getText());
    assertEquals("une valeur", app.getComponent().getVal1().getText());
    assertEquals("Etiquette 2:", app.getComponent().getLabel2().getText());
    assertEquals("valeur de deux", app.getComponent().getVal2().getText());
    assertEquals("email:", app.getComponent().getEmailLabel().getText());
    assertEquals("Entrez votre adresse e-mail...", app.getComponent().getEmail().getElement().getAttribute("placeholder"));
    assertEquals("mot de passe:", app.getComponent().getPasswordLabel().getText());
    assertEquals("Votre mot de passe va ici.", app.getComponent().getPassword().getElement().getAttribute("title"));
  }

}