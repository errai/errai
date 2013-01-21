package org.jboss.errai.ui.test.nested.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.nested.client.res.A;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class NestedTemplateTest extends AbstractErraiCDITest {

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

  /**
   * Regression test for the failure case documented in ERRAI-464.
   */
  @Test
  public void testNestedComponentsWhichBothInjectSomethingCalledAddress() {
    A a = IOC.getBeanManager().lookupBean(A.class).getInstance();
    assertNotNull(a);

    System.out.println(a.getElement().getInnerHTML());
    assertEquals("This is the address field in A.html", a.getAddress().getValue());
    assertEquals("This is the address field in B.html", a.getB().getAddress().getText());
  }

}