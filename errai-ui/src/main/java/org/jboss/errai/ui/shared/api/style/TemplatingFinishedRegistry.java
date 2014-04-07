package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.user.client.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ClassDescription for TemplatingFinishedRegistry
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
public class TemplatingFinishedRegistry {

  private static final TemplatingFinishedRegistry INSTANCE = new TemplatingFinishedRegistry();

  private final Map<Class<? extends Annotation>, List<TemplateFinishedElementExecutor>> finishedCallbacks = new HashMap<Class<? extends Annotation>, List<TemplateFinishedElementExecutor>>();
  private final HashMap<Object, HashMap<Element, List<Annotation>>> beanExecutorMapping = new HashMap<Object, HashMap<Element, List<Annotation>>>();
  private final List<Object> finishedExistingBeans = new ArrayList<Object>();

  private TemplatingFinishedRegistry() {
  }

  public static TemplatingFinishedRegistry get() {
    return INSTANCE;
  }

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

  public void addTemplatingFinishedExecutor(
          final TemplateFinishedElementExecutor templateFinishedExecutor) {
    if (!finishedCallbacks.containsKey(templateFinishedExecutor
            .getTargetAnnotationType())) {
      finishedCallbacks.put(templateFinishedExecutor.getTargetAnnotationType(),
              new ArrayList<TemplateFinishedElementExecutor>());
    }
    finishedCallbacks.get(templateFinishedExecutor.getTargetAnnotationType())
            .add(templateFinishedExecutor);

    callFinishForNewExecutor(templateFinishedExecutor);
  }

  public void removeTemplatingFinishedExecutor(
          final TemplateFinishedElementExecutor templateFinishedExecutor) {
    if (finishedCallbacks.containsKey(templateFinishedExecutor
            .getTargetAnnotationType())) {
      finishedCallbacks.get(templateFinishedExecutor.getTargetAnnotationType())
              .remove(templateFinishedExecutor);
    }
  }

  public void addBeanElement(Object beanInst, Element element,
          Annotation annotation) {
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

  public void removeTemplatedBean(Object beanInst) {
    if (beanExecutorMapping.containsKey(beanInst)) {
      finishedExistingBeans.remove(beanInst);
      beanExecutorMapping.remove(beanInst);
    }
  }

  private void callFinishForNewExecutor(
          TemplateFinishedElementExecutor templateFinishedExecutor) {
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
