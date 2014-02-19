package org.jboss.errai.ui.test.nestedcyclic.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.RegExp;

public class NestedCyclicTemplateTest extends AbstractErraiCDITest {

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

}