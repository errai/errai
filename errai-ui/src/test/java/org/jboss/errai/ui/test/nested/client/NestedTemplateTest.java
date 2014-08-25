package org.jboss.errai.ui.test.nested.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.nested.client.res.A;
import org.jboss.errai.ui.test.nested.client.res.NestedClassComponent;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.RegExp;

public class NestedTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplaceNested() {
    NestedTemplateTestApp app = IOC.getBeanManager().lookupBean(NestedTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());
    String innerHtml = app.getComponent().getElement().getInnerHTML();
    assertTrue(RegExp.compile("<h1(.)*>This will be rendered</h1>").test(innerHtml));
    assertTrue(RegExp.compile("<div(.)*>This will be rendered</div>").test(innerHtml));
    assertTrue(innerHtml.contains("This will be rendered inside button"));
    
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

    assertEquals("This is the address field in A.html", a.getAddress().getValue());
    assertEquals("This is the address field in B.html", a.getB().getAddress().getText());
  }
  
  /**
   * Regression test for the failure case documented in ERRAI-790.
   */
  @Test
  public void testComponentAsStaticInnerClass() {
    NestedClassComponent.Content c = IOC.getBeanManager().lookupBean(NestedClassComponent.Content.class).getInstance();
    assertNotNull(c);
  }

}