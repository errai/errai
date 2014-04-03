package org.jboss.errai.ui.test.template.finished.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Element;
import java.lang.annotation.Annotation;
import static junit.framework.Assert.assertTrue;
import org.jboss.errai.ui.shared.api.style.TemplateFinishedElementExecutor;
import org.jboss.errai.ui.shared.api.style.TemplatingFinishedRegistry;
import org.jboss.errai.ui.test.template.finished.client.res.AddClassNameAnnotation;

public class ElementTemplateFinishedTest extends AbstractErraiCDITest {
  private static int invokeTimes = 0;

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testTemplatingFinishInvokedDirectly() {
    System.out.println("Start testing template finished stuff");
    TemplatingFinishedRegistry.get().addTemplatingFinishedExecutor(AddClassNameAnnotation.class, createTestExecutor());
    ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(ElementTemplateTestApp.class).getInstance();

    Element annotatedElement = app.getForm().getUsername().getElement();
    System.out.println("Annotated Element class name => " + annotatedElement.getClassName());
    assertTrue(annotatedElement.getClassName().endsWith("testing-classname"));

    // IOC.getBeanManager().destroyBean(app);
    app = IOC.getBeanManager().lookupBean(ElementTemplateTestApp.class).newInstance();

    assertEquals(2, invokeTimes);
  }

  private TemplateFinishedElementExecutor createTestExecutor() {
    return new TemplateFinishedElementExecutor() {

      @Override
      public void invoke(Element element, Annotation annoation) {
        invokeTimes++;
        System.out.println("INVOKE IS CALLED!"); // TODO: Remove
        if (!(annoation instanceof AddClassNameAnnotation)) {
          System.err.println("In ElementTemplateFinishedTest the given annotation in invoke has an incorrect type. Expected AddClassNameAnnotation but is "
              + annoation.getClass().getName());
        }
        AddClassNameAnnotation addClassNameAnnotation = (AddClassNameAnnotation) annoation;
        element.addClassName(addClassNameAnnotation.classname());
      }
    };
  }
}
