package org.jboss.errai.ui.test.path.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class PathTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testRelativePathTemplate() {
    PathTemplateTestApp app = IOC.getBeanManager().lookupBean(PathTemplateTestApp.class).getInstance();
    assertNotNull(app.getRelativeComponent());
    assertTrue(app.getRelativeComponent().getElement().getInnerHTML().contains("<h1>This will be rendered</h1>"));
    assertTrue(app.getRelativeComponent().getElement().getInnerHTML().contains("<div>This will be rendered</div>"));
    assertTrue(app.getRelativeComponent().getElement().getInnerHTML().contains("This will be rendered inside button"));

    Element c1 = Document.get().getElementById("c1");
    assertNotNull(c1);
    assertEquals("Added by component", c1.getInnerText());

    assertNotNull(Document.get().getElementById("c2"));
    assertNotNull(Document.get().getElementById("c3"));
    assertNull(Document.get().getElementById("content"));
  }

  @Test
  public void testAbsolutePathTemplate() {
    PathTemplateTestApp app = IOC.getBeanManager().lookupBean(PathTemplateTestApp.class).getInstance();
    assertNotNull(app.getAbsoluteComponent());
    assertTrue(app.getAbsoluteComponent().getElement().getInnerHTML().contains("<h1>This will be rendered</h1>"));
    assertTrue(app.getAbsoluteComponent().getElement().getInnerHTML().contains("<div>This will be rendered</div>"));
    assertTrue(app.getAbsoluteComponent().getElement().getInnerHTML().contains("This will be rendered inside button"));

    Element c1 = Document.get().getElementById("c1");
    assertNotNull(c1);
    assertEquals("Added by component", c1.getInnerText());

    assertNotNull(Document.get().getElementById("c2"));
    assertNotNull(Document.get().getElementById("c3"));
    assertNull(Document.get().getElementById("content"));
  }

}