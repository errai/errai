package org.jboss.errai.ui.test.template.finished.client;

import com.google.gwt.dom.client.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertTrue;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.shared.api.style.TemplateFinishedElementExecutor;
import org.jboss.errai.ui.shared.api.style.TemplatingFinishedRegistry;
import org.jboss.errai.ui.test.template.finished.client.res.AddClassNameAnnotation;
import org.jboss.errai.ui.test.template.finished.client.res.ElementFormComponent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ElementTemplateFinishedTest extends AbstractErraiCDITest {
  private int invokeTimes = 0;
  private List<TemplateFinishedElementExecutor> executors;
  private boolean addClass;

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Before
  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
    addClass = true;
    invokeTimes = 0;
    executors = new ArrayList<TemplateFinishedElementExecutor>();
  }

  @After
  @Override
  public void gwtTearDown() throws Exception {
    for (TemplateFinishedElementExecutor executor : executors) {
      TemplatingFinishedRegistry.get().removeTemplatingFinishedExecutor(
              executor);
    }
    super.gwtTearDown();
  }

  @Test
  public void testTemplatingFinishInvoked() {
    TemplatingFinishedRegistry.get().addTemplatingFinishedExecutor(
            createTestExecutor());

    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).newInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(1, invokeTimes);
    IOC.getBeanManager().destroyBean(form);
  }

  @Test
  public void testTemplateRemove() {
    System.out.println("====> Start Remove");
    TemplateFinishedElementExecutor executor = createTestExecutor();
    TemplatingFinishedRegistry.get().addTemplatingFinishedExecutor(executor);
    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    System.out.println("====> Assert Remove");
    assertEquals(1, invokeTimes);

    ElementFormComponent secondForm = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(secondForm.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(2, invokeTimes);

    TemplatingFinishedRegistry.get().removeTemplatingFinishedExecutor(executor);

    // If we finish an destroyed bean it should do nothing and the count remain
    // on 2
    TemplatingFinishedRegistry.get().templatingFinished(secondForm);

    assertEquals(2, invokeTimes);
    IOC.getBeanManager().destroyBean(form);
    IOC.getBeanManager().destroyBean(secondForm);
  }

  @Test
  public void testRefreshWithTemplateFinished() {
    TemplateFinishedElementExecutor executor = createTestExecutor();
    TemplatingFinishedRegistry.get().addTemplatingFinishedExecutor(executor);
    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(1, invokeTimes);

    addClass = false;
    TemplatingFinishedRegistry.get().templatingFinished(form);

    assertFalse(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(2, invokeTimes);
    IOC.getBeanManager().destroyBean(form);
  }

  private TemplateFinishedElementExecutor createTestExecutor() {
    TemplateFinishedElementExecutor executor = new TemplateFinishedElementExecutor() {

      @Override
      public void invoke(Element element, Annotation annoation) {
        invokeTimes++;
        System.out.println("INVOKE IS CALLED! "
                + annoation.getClass().getName()); // TODO: Remove
        if (!(annoation instanceof AddClassNameAnnotation)) {
          System.err
                  .println("In ElementTemplateFinishedTest the given annotation in invoke has an incorrect type. Expected AddClassNameAnnotation but is "
                          + annoation.getClass().getName());
        }
        AddClassNameAnnotation addClassNameAnnotation = (AddClassNameAnnotation) annoation;
        if (addClass) {
          element.addClassName(addClassNameAnnotation.classname());
        }
        else {
          element.removeClassName(addClassNameAnnotation.classname());
        }
      }

      @Override
      public Class<? extends Annotation> getTargetAnnotationType() {
        return AddClassNameAnnotation.class;
      }
    };
    executors.add(executor);
    return executor;
  }
}
