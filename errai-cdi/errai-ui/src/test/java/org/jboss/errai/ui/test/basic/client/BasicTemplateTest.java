package org.jboss.errai.ui.test.basic.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Anchor;

public class BasicTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplace() {
    BasicTemplateTestApp app = IOC.getBeanManager().lookupBean(BasicTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());
    assertTrue(app.getComponent().getElement().getInnerHTML().contains("<h1>This will be rendered</h1>"));
    assertTrue(app.getComponent().getElement().getInnerHTML().contains("<div>This will be rendered</div>"));
    assertFalse(app.getComponent().getElement().getInnerHTML().contains("This will not be rendered"));

    Element c1 = Document.get().getElementById("c1");
    assertNotNull(c1);
    assertEquals("Added by component", c1.getInnerText());

    assertNotNull(Document.get().getElementById("c2"));
    assertNotNull(Document.get().getElementById("c3"));
    assertNull(Document.get().getElementById("content"));
  }

  @Test
  public void testAttributesFromTemplateOverrideComponentElement() {
    BasicTemplateTestApp app = IOC.getBeanManager().lookupBean(BasicTemplateTestApp.class).getInstance();
    
    Element c1 = app.getComponent().getLabel().getElement();
    assertEquals("something", c1.getAttribute("class"));
    assertEquals("left", c1.getAttribute("align"));
    assertEquals("c1", c1.getAttribute("data-field"));

    Element c3 = app.getComponent().getTextBox().getElement();
    assertEquals("c3", c3.getAttribute("data-field"));
    assertEquals("address", c3.getAttribute("name"));

    Anchor c4comp = app.getComponent().getC4();
    assertEquals("Inner HTML should be preserved when component implements ", "<div>LinkHTML</div>", c4comp.getHTML());
    Element c4 = c4comp.getElement();
    assertEquals("c4", c4.getAttribute("data-field"));
    assertEquals("blah", c4.getAttribute("href"));
    assertEquals("DIV", c4.getFirstChildElement().getTagName());
    assertEquals("LinkHTML", c4.getFirstChildElement().getInnerHTML());
  }

}