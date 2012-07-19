package org.jboss.errai.ui.test.quickhandler.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Document;

public class QuickHandlerTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplace() {
    QuickHandlerTemplateTestApp app = IOC.getBeanManager().lookupBean(QuickHandlerTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());

    ButtonElement c2 = ButtonElement.as(Document.get().getElementById("c2"));
    assertNotNull(c2);

    assertFalse(app.getComponent().isClicked());
    c2.click();
    assertTrue(app.getComponent().isClicked());
  }

}