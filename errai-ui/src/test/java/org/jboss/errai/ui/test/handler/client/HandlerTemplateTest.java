package org.jboss.errai.ui.test.handler.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class HandlerTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplace() {
    final HandlerTemplateTestApp app = IOC.getBeanManager().lookupBean(HandlerTemplateTestApp.class).getInstance();
    assertNotNull(app.getComponent());

    System.out.println("DUMPING: " + Document.get().getElementById("root").getInnerHTML());

    assertNotNull(Document.get().getElementById("b1"));
    assertEquals(Document.get().getElementById("b1"), app.getComponent().getB1().getElement());
    app.getComponent().getB1().click();
    assertNull(Document.get().getElementById("b1"));

    assertNotNull(Document.get().getElementById("b2"));
    app.getComponent().getB2().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        System.out.println("Handled click event on b1.");
        app.getComponent().getB2().removeFromParent();
      }
    });

    app.getComponent().getB2().click();

    assertNull(Document.get().getElementById("b2"));
    assertNotNull(Document.get().getElementById("b3"));
  }
}