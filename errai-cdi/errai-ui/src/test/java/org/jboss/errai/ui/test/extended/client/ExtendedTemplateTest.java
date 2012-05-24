package org.jboss.errai.ui.test.extended.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;

public class ExtendedTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplaceNested() {
    ExtendedTemplateTestApp app = IOC.getBeanManager().lookupBean(ExtendedTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());

    assertNotNull(Document.get().getElementById("root"));
    assertNotNull(Document.get().getElementById("c1"));
    assertNotNull(Document.get().getElementById("c2"));
    assertNotNull(Document.get().getElementById("c3"));
    
    assertFalse(app.getComponent().getElement().getInnerHTML().contains("This will not be rendered"));
  }

}