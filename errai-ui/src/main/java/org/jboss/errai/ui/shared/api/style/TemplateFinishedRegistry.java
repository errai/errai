package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.user.client.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ClassDescription for TemplateFinishedRegistry. Holds all
 * {@link TemplateFinishedElementExecutor} executors. If the Page's elements are
 * initialized with the Template the executors are called for every
 * {@link Element}'s Annotation that has an executor. If there are more than one
 * executor for an Annotation both are invoked. And if there is more than one
 * supported Annotation on an element both executors with their supported
 * Annotation are invoked.
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
public class TemplateFinishedRegistry {

  private static final TemplateFinishedRegistry INSTANCE = new TemplateFinishedRegistry();

  private final Map<Class<? extends Annotation>, List<TemplateFinishedElementExecutor>> finishedCallbacks = new HashMap<Class<? extends Annotation>, List<TemplateFinishedElementExecutor>>();
  private final HashMap<Object, HashMap<Element, List<Annotation>>> beanExecutorMapping = new HashMap<Object, HashMap<Element, List<Annotation>>>();
  private final List<Object> finishedExistingBeans = new ArrayList<Object>();

  private TemplateFinishedRegistry() {
  }

  public static TemplateFinishedRegistry get() {
    return INSTANCE;
  }

  /**
   * This is automatically called when the templating is done.
   * @param beanInst Instance of the page
   */
  public void templatingFinished(Object beanInst) {
    if (beanExecutorMapping.containsKey(beanInst)) {
      if (!finishedExistingBeans.contains(beanInst)) {
        finishedExistingBeans.add(beanInst);
      }
      for (Entry<Element, List<Annotation>> elementExecutorTuple : beanExecutorMapping
              .get(beanInst).entrySet()) {
        for (Annotation elmAnnotation : elementExecutorTuple.getValue()) {
          List<TemplateFinishedElementExecutor> executors = finishedCallbacks
                  .get(elmAnnotation.annotationType());
          if (executors != null) {
            for (TemplateFinishedElementExecutor executor : executors) {
              executor.invoke(elementExecutorTuple.getKey(), elmAnnotation);
            }
          }
        }
      }
    }
  }

  /**
   * This methods registers a {@link TemplateFinishedElementExecutor}. If there are already Pages that are done have
   * annotated elements for the Annotation then the executor is called immediately. It's possible to register multiple
   * executors for the same Annotation. The registered executor will be called for every element with the supported
   * annotation.
   *
   * @param templateFinishedExecutor 
   */
  public void addTemplatingFinishedExecutor(final TemplateFinishedElementExecutor templateFinishedExecutor) {
    if (!finishedCallbacks.containsKey(templateFinishedExecutor.getTargetAnnotationType())) {
      finishedCallbacks.put(templateFinishedExecutor.getTargetAnnotationType(),
              new ArrayList<TemplateFinishedElementExecutor>());
    }

    finishedCallbacks.get(templateFinishedExecutor.getTargetAnnotationType())
            .add(templateFinishedExecutor);

    callFinishForNewExecutor(templateFinishedExecutor);
  }

  /**
   * Remove an registered {@link TemplateFinishedElementExecutor}.
   *
   * @param templateFinishedExecutor 
   */
  public void removeTemplatingFinishedExecutor(final TemplateFinishedElementExecutor templateFinishedExecutor) {
    if (finishedCallbacks.containsKey(templateFinishedExecutor.getTargetAnnotationType())) {
      finishedCallbacks.get(templateFinishedExecutor.getTargetAnnotationType())
              .remove(templateFinishedExecutor);
    }
  }

  /**
   * This is automatically called for every Annotated field. Register's an element of a page and it's Annotation.
   * Multiple Annotations for the same element are supported.
   *
   * @param beanInst
   * @param element
   * @param annotation 
   */
  public void addBeanElement(Object beanInst, Element element, Annotation annotation) {
    if (!beanExecutorMapping.containsKey(beanInst)) {
      beanExecutorMapping.put(beanInst,
              new HashMap<Element, List<Annotation>>());
    }
    List<TemplateFinishedElementExecutor> executors = finishedCallbacks
            .get(annotation.annotationType());
    if (executors == null) {
      executors = new ArrayList<TemplateFinishedElementExecutor>();
      finishedCallbacks.put(annotation.getClass(), executors);
    }
    Map<Element, List<Annotation>> beanElements = beanExecutorMapping
            .get(beanInst);
    if (!beanElements.containsKey(element)) {
      beanElements.put(element, new ArrayList<Annotation>());
    }
    if (!beanExecutorMapping.get(beanInst).get(element).contains(annotation)) {
      beanExecutorMapping.get(beanInst).get(element).add(annotation);
    }
  }

  /**
   * This is automatically called if the bean will be destroyed. It removes the page instance from the list of
   * registered instances.
   *
   * @param beanInst 
   */
  public void removeTemplatedBean(Object beanInst) {
    if (beanExecutorMapping.containsKey(beanInst)) {
      finishedExistingBeans.remove(beanInst);
      beanExecutorMapping.remove(beanInst);
    }
  }

  /**
   * Is called if an executor is added to check if there are already elements that should have called it.
   *
   * @param templateFinishedExecutor 
   */
  private void callFinishForNewExecutor(TemplateFinishedElementExecutor templateFinishedExecutor) {
    for (Object finishedBean : finishedExistingBeans) {
      for (Entry<Element, List<Annotation>> elementEntry : beanExecutorMapping
              .get(finishedBean).entrySet()) {
        for (Annotation annotation : elementEntry.getValue()) {
          if (annotation.annotationType().equals(
                  templateFinishedExecutor.getTargetAnnotationType())) {
            templateFinishedExecutor.invoke(elementEntry.getKey(), annotation);
          }
        }
      }
    }
  }
}