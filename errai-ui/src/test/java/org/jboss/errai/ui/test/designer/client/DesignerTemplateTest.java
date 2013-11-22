package org.jboss.errai.ui.test.designer.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;

public class DesignerTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplaceNested() {
    DesignerTemplateTestApp app = IOC.getBeanManager().lookupBean(DesignerTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());
    System.out.println(app.getRoot().getElement().getInnerHTML());

    assertNotNull(Document.get().getElementById("btn"));
    assertEquals("Will be rendered inside button", app.getComponent().getButton().getElement().getInnerHTML());
    assertNotNull(Document.get().getElementById("somethingNew"));
    assertNotNull(Document.get().getElementById("basic"));
    assertNotNull(Document.get().getElementById("h2"));
  }
  
  @Test
  public void testInsertAndReplaceNestedUsingIdsAndClasses() {
    DesignerTemplateTestAppUsingIdsAndClasses app = IOC.getBeanManager().lookupBean(DesignerTemplateTestAppUsingIdsAndClasses.class).getInstance();
    assertNotNull(app.getComponent());
    System.out.println(app.getRoot().getElement().getInnerHTML());

    assertNotNull(Document.get().getElementById("btn"));
    assertEquals("Will be rendered inside button", app.getComponent().getButton().getElement().getInnerHTML());
    assertNotNull(Document.get().getElementById("somethingNew"));
    assertNotNull(Document.get().getElementById("basic"));
    assertNotNull(Document.get().getElementById("h2"));
  }

}