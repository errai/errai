package org.jboss.errai.ui.test.element.client;

import static elemental.client.Browser.getDocument;

import org.jboss.errai.common.client.ui.ElementWrapperWidget;
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
    final ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(CompositeElementTemplateTestApp.class).getInstance();
    assertContentIsCorrect(app);
  }

  @Test
  public void testUseElementInNonCompositeComponent() {
    final ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(NonCompositeElementTemplateTestApp.class).getInstance();
    assertContentIsCorrect(app);
  }

  @Test
  public void testElementWrapperWidgetIsRemovedOnDestructionInNonCompositeTemplate() throws Exception {
    final ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(NonCompositeElementTemplateTestApp.class).getInstance();
    assertWrapperWidgetIsRemovedOnDestruction(app);
  }

  @Test
  public void testElementWrapperWidgetIsRemovedOnDestructionInCompositeTemplate() throws Exception {
    final ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(CompositeElementTemplateTestApp.class).getInstance();
    assertWrapperWidgetIsRemovedOnDestruction(app);
  }

  private void assertWrapperWidgetIsRemovedOnDestruction(final ElementTemplateTestApp app) {
    final Element formElement = app.getForm().getForm();
    final ElementWrapperWidget wrapper = ElementWrapperWidget.getWidget(formElement);
    assertSame("Control failed: This method should return the same instance until it is removed.", wrapper, ElementWrapperWidget.getWidget(formElement));
    IOC.getBeanManager().destroyBean(app);
    assertNotSame("Wrapper widget was not removed.", wrapper, ElementWrapperWidget.getWidget(formElement));
  }

  private void assertContentIsCorrect(final ElementTemplateTestApp app) {
    final Element form = app.getForm().getElement();
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
  public static void click(final EventTarget target) {
    final MouseEvent evt = (MouseEvent) getDocument().createEvent(
        Document.Events.MOUSE);
    evt.initMouseEvent("click", true, true, null, 0, 0, 0, 0, 0, false, false,
        false, false, MouseEvent.Button.PRIMARY, null);
    target.dispatchEvent(evt);
  }

}