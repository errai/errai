package org.jboss.errai.ui.test.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

public class LoadTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.test.Test";
  }

  @Test
  public void testInsertAndReplace() {
    App app = CDITestHelper.instance.app;
    assertNotNull(app.getComponent());
    assertTrue(app.getComponent().getElement().getInnerHTML().contains("<h1>This will be rendered</h1>"));
    assertTrue(app.getComponent().getElement().getInnerHTML().contains("<div>This will be rendered</div>"));
    assertFalse(app.getComponent().getElement().getInnerHTML().contains("This will not be rendered"));

    Element lbl = Document.get().getElementById("lbl");
    assertNotNull(lbl);
    assertEquals("Added by component", lbl.getInnerText());

    assertNotNull(Document.get().getElementById("btn"));

    assertNull(Document.get().getElementById("content"));
    assertNotNull(Document.get().getElementById("content2"));
  }

}