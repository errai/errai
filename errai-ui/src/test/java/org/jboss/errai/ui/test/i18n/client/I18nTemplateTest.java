package org.jboss.errai.ui.test.i18n.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.UListElement;

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

    assertEquals("Email:", app.getComponent().getEmailLabel().getText());
    assertEquals("Enter your email address...", app.getComponent().getEmail().getElement().getAttribute("placeholder"));
    assertEquals("Password:", app.getComponent().getPasswordLabel().getText());
    assertEquals("Your password goes here.", app.getComponent().getPassword().getElement().getAttribute("title"));

  }

  @Test
  public void testChildrenOfDummyElementAreIgnored() {
    I18nTemplateTestApp app = IOC.getBeanManager().lookupBean(org.jboss.errai.ui.test.i18n.client.I18nTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());

    UListElement variableLengthList = app.getComponent().getVariableLengthList();
    assertNotNull(variableLengthList);

    // NOTE: if ErraiUI is enhanced in the future to completely snip out the children of data-role=dummy elements,
    //       then the following code will fail with NPEs. In that case, this whole test method can be deleted.
    assertEquals("Example Item 1", variableLengthList.getFirstChildElement().getInnerText());
    assertEquals("Second Example Item", variableLengthList.getFirstChildElement().getNextSiblingElement().getInnerText());
  }
}