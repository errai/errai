package org.jboss.errai.ui.test.element.client;

import static elemental.client.Browser.getDocument;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.junit.Test;

import com.google.gwt.dom.client.Element;

import elemental.dom.Document;
import elemental.events.EventTarget;
import elemental.events.MouseEvent;

public class ElementTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testUseElementInCompositeComponent() {
    ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(CompositeElementTemplateTestApp.class).getInstance();
    runAssertions(app);
  }

  @Test
  public void testUseElementInNonCompositeComponent() {
    ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(NonCompositeElementTemplateTestApp.class).getInstance();
    runAssertions(app);
  }

  private void runAssertions(ElementTemplateTestApp app) {
    Element form = app.getForm().getElement();
    assertTrue(form.getInnerHTML().contains("Keep me logged in on this computer"));
    assertTrue(app.getForm().getForm().getInnerHTML().contains("Keep me logged in on this computer"));
    assertEquals("Cancel", app.getForm().getCancel().getTextContent());

    assertEquals("Username", TemplateUtil.asElement(app.getForm().getUsername()).getAttribute("placeholder"));
    // This assertion on it's own could fail if there is a problem with how we use JsInterop
    assertEquals("Username", app.getForm().getUsername().getAttribute("placeholder"));

    assertEquals(0, app.getForm().getNumberOfTimesPressed());
    click(app.getForm().getCancel());
    assertEquals(1, app.getForm().getNumberOfTimesPressed());
  }

  /**
   * Fires a left-click event on the given target (typically a DOM node).
   */
  public static void click(EventTarget target) {
    MouseEvent evt = (MouseEvent) getDocument().createEvent(
        Document.Events.MOUSE);
    evt.initMouseEvent("click", true, true, null, 0, 0, 0, 0, 0, false, false,
        false, false, MouseEvent.Button.PRIMARY, null);
    target.dispatchEvent(evt);
  }

}