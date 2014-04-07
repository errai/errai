package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.user.client.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.errai.ioc.client.container.Tuple;

/**
 * ClassDescription for TemplatingFinishedRegistry
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
public class TemplatingFinishedRegistry {

  private static final TemplatingFinishedRegistry INSTANCE = new TemplatingFinishedRegistry();

  private final Map<Class<? extends Annotation>, List<TemplateFinishedElementExecutor>> finishedCallbacks = new HashMap<Class<? extends Annotation>, List<TemplateFinishedElementExecutor>>();
  private final Map<Object, List<Tuple<Element, List<TemplateFinishedElementExecutor>>>> beanExecutorMapping = new HashMap<Object, List<Tuple<Element, List<TemplateFinishedElementExecutor>>>>();
  // Allow more than one annotation for an element?
  private final Map<Element, List<Annotation>> elementAnnotation = new HashMap<Element, List<Annotation>>();

  private TemplatingFinishedRegistry() {
  }

  public static TemplatingFinishedRegistry get() {
    System.out.println("Templating get called");
    return INSTANCE;
  }

  public void templatingFinished(Object beanInst) {
    System.out.println("Templating finished called => "
            + beanInst.getClass().getName());
    if (beanExecutorMapping.containsKey(beanInst)) {
      System.out.println("Found beanInst");
      for (Tuple<Element, List<TemplateFinishedElementExecutor>> elementExecutorTuple : beanExecutorMapping
              .get(beanInst)) {
        System.out.println(" for elements");
        for (TemplateFinishedElementExecutor executor : elementExecutorTuple
                .getValue()) {
          System.out.println("For values");
          for (Annotation elmAnnotation : elementAnnotation
                  .get(elementExecutorTuple.getKey())) {
            if (executor.getTargetAnnotationType().equals(
                    elmAnnotation.annotationType()))
              executor.invoke(elementExecutorTuple.getKey(), elmAnnotation);
          }
        }
      }
    }
  }

  public void addTemplatingFinishedExecutor(
          final TemplateFinishedElementExecutor templateFinishedExecutor) {
    System.out.println("Templating executor added for "
            + templateFinishedExecutor.getTargetAnnotationType().getName());
    if (!finishedCallbacks.containsKey(templateFinishedExecutor
            .getTargetAnnotationType())) {
      finishedCallbacks.put(templateFinishedExecutor.getTargetAnnotationType(),
              new ArrayList<TemplateFinishedElementExecutor>());
      System.out.println("Added new annotation");
    }
    finishedCallbacks.get(templateFinishedExecutor.getTargetAnnotationType())
            .add(templateFinishedExecutor);

    // Check if there are already elements that should be executed
    for (Entry<Element, List<Annotation>> elementAnnotationEntry : elementAnnotation
            .entrySet()) {
      // System.out.println("Found element and annotation => "
      // + elementAnnotationEntry.getKey().getClassName() + " with "
      // + elementAnnotationEntry.getValue().annotationType().getName());
      for (Annotation elmAnnotation : elementAnnotationEntry.getValue()) {
        if (elmAnnotation.annotationType().equals(
                templateFinishedExecutor.getTargetAnnotationType())) {
          templateFinishedExecutor.invoke(elementAnnotationEntry.getKey(),
                  elmAnnotation);
        }
      }
    }
  }

  public void removeTemplatingFinishedExecutor(
          final TemplateFinishedElementExecutor templateFinishedExecutor) {
    System.out.println("Remove executor");
    if (finishedCallbacks.containsKey(templateFinishedExecutor
            .getTargetAnnotationType())) {
      finishedCallbacks.get(templateFinishedExecutor.getTargetAnnotationType())
              .remove(templateFinishedExecutor);
      System.out.println("Executor REMOVED! "
              + finishedCallbacks.get(
                      templateFinishedExecutor.getTargetAnnotationType())
                      .size());
    }
  }

  public void addBeanElement(Object beanInst, Element element,
          Annotation annotation) {
    if (!beanExecutorMapping.containsKey(beanInst)) {
      beanExecutorMapping
              .put(beanInst,
                      new ArrayList<Tuple<Element, List<TemplateFinishedElementExecutor>>>());
    }
    List<TemplateFinishedElementExecutor> executors = finishedCallbacks
            .get(annotation.annotationType());
    if (executors == null) {
      executors = new ArrayList<TemplateFinishedElementExecutor>();
      finishedCallbacks.put(annotation.getClass(), executors);
    }
    beanExecutorMapping.get(beanInst).add(Tuple.of(element, executors));
    if (!elementAnnotation.containsKey(element)) {
      elementAnnotation.put(element, new ArrayList<Annotation>());
    }
    elementAnnotation.get(element).add(annotation);
  }

  public void removeTemplatedBean(Object beanInst) {
    System.out.println("Remove was called");
    if (beanExecutorMapping.containsKey(beanInst)) {
      for (Tuple<Element, List<TemplateFinishedElementExecutor>> elementExecutorTuple : beanExecutorMapping
              .get(beanInst)) {
        elementAnnotation.remove(elementExecutorTuple.getKey());
      }
      beanExecutorMapping.remove(beanInst);
    }
  }

  public void removeBeanElement(Object beanInst, Element element) {
    if (beanExecutorMapping.containsKey(beanInst)) {
      for (Tuple<Element, List<TemplateFinishedElementExecutor>> elementExecutorTuple : beanExecutorMapping
              .get(beanInst)) {
        if (elementExecutorTuple.getKey() == element) {
          beanExecutorMapping.remove(elementExecutorTuple);
        }
      }
      beanExecutorMapping.remove(beanInst);
    }
    if (elementAnnotation.containsKey(element)) {
      elementAnnotation.remove(element);
    }
  }

}
