package org.jboss.errai.ui.test.nestedcyclic.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class NestedCyclicTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplaceNested() {
    NestedTemplateTestApp app = IOC.getBeanManager().lookupBean(NestedTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());
    System.out.println(app.getComponent().getElement().getInnerHTML());
    assertTrue(app.getComponent().getElement().getInnerHTML().contains("<h1>This will be rendered</h1>"));
    assertTrue(app.getComponent().getElement().getInnerHTML().contains("<div>This will be rendered</div>"));
    assertTrue(app.getComponent().getButton().getElement().getInnerHTML()
            .contains("This will be rendered inside button"));

    Element lbl = Document.get().getElementById("c1a");
    assertNotNull(lbl);
    assertEquals("Added by component", lbl.getInnerText());

    assertNull(Document.get().getElementById("content"));
    assertNotNull(Document.get().getElementById("c1"));
    assertNotNull(Document.get().getElementById("c1a"));
    assertNotNull(Document.get().getElementById("c1b"));
    assertNotNull(Document.get().getElementById("c2"));
  }

}