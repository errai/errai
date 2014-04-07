package org.jboss.errai.ui.test.element.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Element;

public class ElementTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testUseElementDirectly() {
    ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(ElementTemplateTestApp.class).getInstance();

    Element form = app.getForm().getElement();
    assertTrue(form.getInnerHTML().contains("Keep me logged in on this computer"));
  }

}