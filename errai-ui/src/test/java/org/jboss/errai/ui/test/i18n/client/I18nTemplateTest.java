package org.jboss.errai.ui.test.i18n.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.client.widget.LocaleListBox;
import org.jboss.errai.ui.client.widget.LocaleSelector;
import org.jboss.errai.ui.shared.api.Locale;
import org.junit.Test;

public class I18nTemplateTest extends AbstractErraiCDITest {

  private I18nTemplateTestApp app;

  /**
   * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
   */
  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    app = IOC.getBeanManager().lookupBean(I18nTemplateTestApp.class).getInstance();

  }

  /**
   * Tests that the bundle is created and is accessible.
   */
  @Test
  public void testBundleAccess() {
    assertNotNull(app.getComponent());
    assertEquals("Welcome to the errai-ui i18n demo.", app.getComponent().getWelcome_p().getInnerText());
    assertEquals("Label 1:", app.getComponent().getLabel1().getText());
    assertEquals("value one", app.getComponent().getVal1().getText());
    assertEquals("Label 1.1:", app.getComponent().getNestedLabel().getText());
    assertEquals("value one.one", app.getComponent().getVal1_1().getText());
    assertEquals("Label 2:", app.getComponent().getLabel2().getText());
    assertEquals("value two", app.getComponent().getVal2().getText());
    assertEquals("This is a really really really really really really really really really" +
    		" really really really really really really really really really really really" +
    		" long string that exceeds the cutoff length for adding a hash to its internationalization key.",
    		app.getComponent().getLongTextLabel().getText());

    assertEquals("Email:", app.getComponent().getEmailLabel().getText());
    assertEquals("Enter your email address...", app.getComponent().getEmail().getElement().getAttribute("placeholder"));
    assertEquals("Password:", app.getComponent().getPasswordLabel().getText());
    assertEquals("Your password goes here.", app.getComponent().getPassword().getElement().getAttribute("title"));

  }

  @Test
  public void testShouldCreateLocaleListBoxContainingAllLanguageOptions() {
    // given
    LocaleSelector selector = IOC.getBeanManager().lookupBean(LocaleSelector.class).getInstance();
    LocaleListBox localeListBox = app.getComponent().getListBox();
    localeListBox.init();

    // when - then
    assertNull(localeListBox.getValue());
    assertEquals(4, selector.getSupportedLocales().size());

    localeListBox.setValue(new Locale("da", "Danish"), true);

    assertEquals("da", TranslationService.currentLocale());
    assertNotNull(localeListBox.getValue());
  }
}