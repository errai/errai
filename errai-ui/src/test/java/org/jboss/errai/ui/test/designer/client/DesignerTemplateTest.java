package org.jboss.errai.ui.test.designer.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;

public class DesignerTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplaceNested() {
    DesignerTemplateTestApp app = IOC.getBeanManager().lookupBean(DesignerTemplateTestAppUsingDataFields.class).getInstance();
    insertAndReplaceNestedAssertions(app);
    IOC.getBeanManager().destroyBean(app);
  }

  @Test
  public void testInsertAndReplaceNestedUsingIdsAndClasses() {
    DesignerTemplateTestApp app = IOC.getBeanManager().lookupBean(DesignerTemplateTestAppUsingIdsAndClasses.class).getInstance();
    insertAndReplaceNestedAssertions(app);
    IOC.getBeanManager().destroyBean(app);
  }

  @Test
  public void testInsertAndReplaceNestedUsingNonCompositeComponent() {
    DesignerTemplateTestApp app = IOC.getBeanManager().lookupBean(DesignerTemplateTestAppUsingNonCompositeComponent.class).getInstance();
    insertAndReplaceNestedAssertions(app);
    IOC.getBeanManager().destroyBean(app);
  }

  private void insertAndReplaceNestedAssertions(DesignerTemplateTestApp app) {
    assertNotNull("Component was not injected.", app.getComponent());
    final String html = app.getRoot().getElement().getInnerHTML();

    assertNotNull(messageHelper("btn was not found.", html), Document.get().getElementById("btn"));
    assertEquals("Will be rendered inside button", app.getComponent().getButton().getElement().getInnerHTML());
    assertNotNull(messageHelper("somethingNew was not found.", html), Document.get().getElementById("somethingNew"));
    assertNotNull(messageHelper("basic was not found.", html), Document.get().getElementById("basic"));
    assertNotNull(messageHelper("h2 was not found.", html), Document.get().getElementById("h2"));
  }

  private static String messageHelper(final String message, final String html) {
    return message + "\n" + html + "\n";
  }

}