package org.jboss.errai.ui.test.template.finished.client;

import com.google.gwt.dom.client.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertTrue;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.shared.api.style.TemplateFinishedElementExecutor;
import org.jboss.errai.ui.shared.api.style.TemplateFinishedRegistry;
import org.jboss.errai.ui.test.template.finished.client.res.AddClassNameAnnotation;
import org.jboss.errai.ui.test.template.finished.client.res.ElementFormComponent;
import org.jboss.errai.ui.test.template.finished.client.res.ElementFormComponentMultiple;
import org.jboss.errai.ui.test.template.finished.client.res.PermissionAnnotation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ElementTemplateFinishedTest extends AbstractErraiCDITest {

  private static final String PERMISSION_VALUE = "authenticate-user";

  private int invokeTimes = 0;
  private List<TemplateFinishedElementExecutor> executors;
  private boolean addClass;
  private boolean isAllowed;

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Before
  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
    addClass = true;
    isAllowed = true;
    invokeTimes = 0;
    executors = new ArrayList<TemplateFinishedElementExecutor>();
  }

  @After
  @Override
  public void gwtTearDown() throws Exception {
    for (TemplateFinishedElementExecutor executor : executors) {
      TemplateFinishedRegistry.get().removeTemplatingFinishedExecutor(
              executor);
    }
    super.gwtTearDown();
  }

  @Test
  public void testTemplatingFinishInvoked() {
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(
            createAddClassNameExecutor());

    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).newInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(1, invokeTimes);
    IOC.getBeanManager().destroyBean(form);
  }

  @Test
  public void testTemplateRemove() {
    TemplateFinishedElementExecutor executor = createAddClassNameExecutor();
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(executor);
    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(1, invokeTimes);

    ElementFormComponent secondForm = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(secondForm.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(2, invokeTimes);

    TemplateFinishedRegistry.get().removeTemplatingFinishedExecutor(executor);

    // If we finish an destroyed bean it should do nothing and the count remain
    // on 2
    TemplateFinishedRegistry.get().templatingFinished(secondForm);

    assertEquals(2, invokeTimes);
    IOC.getBeanManager().destroyBean(form);
    IOC.getBeanManager().destroyBean(secondForm);
  }

  @Test
  public void testBeanRemovedOnDestruct() {
    TemplateFinishedElementExecutor executor = createAddClassNameExecutor();
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(executor);
    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(1, invokeTimes);
    IOC.getBeanManager().destroyBean(form);

    TemplateFinishedRegistry.get().templatingFinished(form);

    assertEquals(1, invokeTimes);
  }

  @Test
  public void testRefreshWithTemplateFinished() {
    TemplateFinishedElementExecutor executor = createAddClassNameExecutor();
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(executor);
    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(1, invokeTimes);

    addClass = false;
    TemplateFinishedRegistry.get().templatingFinished(form);

    assertFalse(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));
    assertEquals(2, invokeTimes);
    IOC.getBeanManager().destroyBean(form);
  }

  @Test
  public void testAddExecutorForAlreadyFinishedTemplate() {
    TemplateFinishedElementExecutor executor = createAddClassNameExecutor();
    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertEquals(0, invokeTimes);

    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(executor);

    assertEquals(1, invokeTimes);
    assertTrue(form.getUsername().getElement().getClassName()
            .endsWith("testing-classname"));

    IOC.getBeanManager().destroyBean(form);
  }

  @Test
  public void testMultipleExecutorsForSameAnnotation() {
    TemplateFinishedElementExecutor classNameExecutor = createAddClassNameExecutor();
    TemplateFinishedElementExecutor secondClassNameExecutor = createAddClassNameExecutor();
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(
            secondClassNameExecutor);
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(
            classNameExecutor);

    ElementFormComponent form = IOC.getBeanManager()
            .lookupBean(ElementFormComponent.class).getInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .contains("testing-classname"));
    assertEquals(2, invokeTimes);

    IOC.getBeanManager().destroyBean(form);
  }

  @Test
  public void testMultipleAnnotations() {
    TemplateFinishedElementExecutor classNameExecutor = createAddClassNameExecutor();
    TemplateFinishedElementExecutor permissionExecutor = createPermissionExecutor(PERMISSION_VALUE);
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(
            classNameExecutor);
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(
            permissionExecutor);

    ElementFormComponentMultiple form = IOC.getBeanManager()
            .lookupBean(ElementFormComponentMultiple.class).getInstance();

    assertFalse(form.getUsername().getElement().getClassName()
            .contains("disabled"));
    assertTrue(form.getPassword().getElement().getClassName()
            .contains("disabled"));
    assertTrue(form.getUsername().getElement().getClassName()
            .contains("testing-classname"));
    assertEquals(3, invokeTimes);

    IOC.getBeanManager().destroyBean(form);
  }

  @Test
  public void testMultipleAnnotationsPermissionDenied() {
    TemplateFinishedElementExecutor classNameExecutor = createAddClassNameExecutor();
    TemplateFinishedElementExecutor permissionExecutor = createPermissionExecutor("authenticate-password");
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(
            classNameExecutor);
    TemplateFinishedRegistry.get().addTemplatingFinishedExecutor(
            permissionExecutor);

    ElementFormComponentMultiple form = IOC.getBeanManager()
            .lookupBean(ElementFormComponentMultiple.class).getInstance();

    assertTrue(form.getUsername().getElement().getClassName()
            .contains("disabled"));
    assertFalse(form.getPassword().getElement().getClassName()
            .contains("disabled"));
    assertTrue(form.getUsername().getElement().getClassName()
            .contains("testing-classname"));
    assertEquals(3, invokeTimes);

    IOC.getBeanManager().destroyBean(form);
  }

  private TemplateFinishedElementExecutor createPermissionExecutor(
          final String expectedPermission) {
    TemplateFinishedElementExecutor executor = new TemplateFinishedElementExecutor() {

      @Override
      public void invoke(Element element, Annotation annoation) {
        invokeTimes++;
        PermissionAnnotation permissionAnnotation = (PermissionAnnotation) annoation;
        if (!expectedPermission.equals(permissionAnnotation.value())) {
          element.addClassName("disabled");
        }
        else {
          element.removeClassName("disabled");
        }
      }

      @Override
      public Class<? extends Annotation> getTargetAnnotationType() {
        return PermissionAnnotation.class;
      }
    };
    executors.add(executor);
    return executor;
  }

  private TemplateFinishedElementExecutor createAddClassNameExecutor() {
    TemplateFinishedElementExecutor executor = new TemplateFinishedElementExecutor() {

      @Override
      public void invoke(Element element, Annotation annoation) {
        invokeTimes++;
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
