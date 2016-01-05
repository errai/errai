package org.jboss.errai.ui.test.basic.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.basic.client.res.StyledComponent;
import org.jboss.errai.ui.test.basic.client.res.StyledComponentWithAbsoluteSheetPath;
import org.jboss.errai.ui.test.basic.client.res.StyledComponentWithRelativeSheetPath;
import org.jboss.errai.ui.test.basic.client.res.StyledTemplatedBean;
import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;

public class BasicTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplace() {
    BasicTemplateTestApp app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingDataFields.class).getInstance();
    testInsertAndReplace(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingIds.class).getInstance();
    testInsertAndReplace(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingStyleClasses.class).getInstance();
    testInsertAndReplace(app);
  }

  private void testInsertAndReplace(BasicTemplateTestApp app) {
    assertNotNull(app.getComponent());
    String innerHtml = app.getComponent().getElement().getInnerHTML();
    assertTrue(RegExp.compile("<h1(.)*>This will be rendered</h1>").test(innerHtml));
    assertTrue(RegExp.compile("<div(.)*>This will be rendered</div>").test(innerHtml));
    assertTrue(innerHtml.contains("This will be rendered inside button"));

    Element c1 = Document.get().getElementById("c1");
    assertNotNull(c1);
    assertEquals("Added by component", c1.getInnerText());

    assertNotNull(Document.get().getElementById("c2"));
    assertNotNull(Document.get().getElementById("c3"));
    assertNull(Document.get().getElementById("content"));
  }

  @Test
  public void testAttributesFromTemplateOverrideComponentElement() {
    BasicTemplateTestApp app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingDataFields.class).getInstance();
    testAttributesFromTemplateOverrideComponentElement(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingIds.class).getInstance();
    testAttributesFromTemplateOverrideComponentElement(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingStyleClasses.class).getInstance();
    testAttributesFromTemplateOverrideComponentElement(app);
  }

  private void testAttributesFromTemplateOverrideComponentElement(BasicTemplateTestApp app) {
    Element c1 = app.getComponent().getLabel().getElement();
    assertEquals("c1", c1.getAttribute("class"));
    assertEquals("left", c1.getAttribute("align"));

    Element c3 = app.getComponent().getTextBox().getElement();
    assertEquals("address", c3.getAttribute("name"));
  }

  @Test
  public void testHasHTMLPreservesInnerHTML() throws Exception {
    BasicTemplateTestApp app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingDataFields.class).getInstance();
    testHasHTMLPreservesInnerHTML(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingIds.class).getInstance();
    testHasHTMLPreservesInnerHTML(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingStyleClasses.class).getInstance();
    testHasHTMLPreservesInnerHTML(app);
  }

  private void testHasHTMLPreservesInnerHTML(BasicTemplateTestApp app) throws Exception {
    Anchor c4comp = app.getComponent().getC4();
    String c4compHtml = c4comp.getHTML();
    assertTrue("Inner HTML should be preserved when component implements ",
        RegExp.compile("<span(.)*>LinkHTML</span>").test(c4compHtml));

    Element c4 = c4comp.getElement();
    assertEquals("blah", c4.getAttribute("href"));
    assertEquals("SPAN", c4.getFirstChildElement().getTagName());
    assertEquals("LinkHTML", c4.getFirstChildElement().getInnerHTML());
  }

  @Test
  public void testHasHTMLReparentsChildElements() throws Exception {
    BasicTemplateTestApp app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingDataFields.class).getInstance();
    testHasHTMLReparentsChildElements(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingIds.class).getInstance();
    testHasHTMLReparentsChildElements(app);
    app = IOC.getBeanManager().lookupBean(BasicTemplateTestAppUsingStyleClasses.class).getInstance();
    testHasHTMLReparentsChildElements(app);
  }

  private void testHasHTMLReparentsChildElements(BasicTemplateTestApp app) throws Exception {
    Anchor c5 = app.getComponent().getC5();
    Image c6 = app.getComponent().getC6();

    System.out.println("DUMPING: " + Document.get().getElementById("root").getInnerHTML());
    assertEquals(c6.getElement(), c5.getElement().getFirstChildElement());
  }

  @Test
  public void testPrecedenceRules() throws Exception {
    PrecedenceTemplateTestApp app = IOC.getBeanManager().lookupBean(PrecedenceTemplateTestApp.class).getInstance();

    assertEquals(app.getComponent().getA().getText(), "This is a");
    assertEquals(app.getComponent().getB().getText(), "This is b");
    assertEquals(app.getComponent().getC().getText(), "This is c");
    assertEquals(app.getComponent().getE().getText(), "This is d, e, and f");
  }

  public void testStyleSheetWithDefaultPath() throws Exception {
    final StyledTemplatedBean bean = IOC.getBeanManager().lookupBean(StyledComponent.class).getInstance();
    styledBeanAssertions(bean, "font-size", "100px");
  }

  public void testStyleSheetWithRelativePath() throws Exception {
    final StyledTemplatedBean bean = IOC.getBeanManager().lookupBean(StyledComponentWithRelativeSheetPath.class).getInstance();
    styledBeanAssertions(bean, "font-color", "red");
  }

  public void testStyleSheetWithAbsolutePath() throws Exception {
    final StyledTemplatedBean bean = IOC.getBeanManager().lookupBean(StyledComponentWithAbsoluteSheetPath.class).getInstance();
    styledBeanAssertions(bean, "margin", "10px");
  }

  private void styledBeanAssertions(final StyledTemplatedBean bean, final String propertyName, final String propertyValue) {
    // Need to flush so that styles are computed immediately.
    StyleInjector.flush();
    assertTrue("The span element did not receive the class attribute from the template.", bean.getStyled().hasClassName("styled"));
    assertEquals("Style from StyledComponent.css was not applied.", propertyValue, getPropertyValue(bean.getStyled(), propertyName));
  }

  private static native String getPropertyValue(Element elem, String prop) /*-{
    return $wnd.getComputedStyle(elem,null).getPropertyValue(prop);
  }-*/;

}