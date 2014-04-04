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
  private final Map<Element, Annotation> elementAnnotation = new HashMap<Element, Annotation>();

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
      for (Tuple<Element, List<TemplateFinishedElementExecutor>> elementExecutorTuple : beanExecutorMapping
              .get(beanInst)) {
        for (TemplateFinishedElementExecutor executor : elementExecutorTuple
                .getValue()) {
          executor.invoke(elementExecutorTuple.getKey(),
                  elementAnnotation.get(elementExecutorTuple.getKey()));
        }
      }
    }
  }

  public void addTemplatingFinishedExecutor(
          final Class<? extends Annotation> annotation,
          final TemplateFinishedElementExecutor templateFinishedExecutor) {
    System.out.println("Templating executor added for " + annotation.getName());
    if (!finishedCallbacks.containsKey(annotation)) {
      finishedCallbacks.put(annotation,
              new ArrayList<TemplateFinishedElementExecutor>());
    }
    finishedCallbacks.get(annotation).add(templateFinishedExecutor);

    // Check if there are already elements that should be executed
    for (Entry<Element, Annotation> elementAnnotationEntry : elementAnnotation
            .entrySet()) {
      System.out.println("Found element and annotation => "
              + elementAnnotationEntry.getKey().getClassName() + " with "
              + elementAnnotationEntry.getValue().annotationType().getName());
      if (elementAnnotationEntry.getValue().annotationType().equals(annotation)) {
        templateFinishedExecutor.invoke(elementAnnotationEntry.getKey(),
                elementAnnotationEntry.getValue());
      }
    }
  }

  public void addBeanElement(Object beanInst, Element element,
          Annotation annotation) {
    System.out.println("Templating add element for "
            + annotation.getClass().getName());
    if (!beanExecutorMapping.containsKey(beanInst)) {
      beanExecutorMapping
              .put(beanInst,
                      new ArrayList<Tuple<Element, List<TemplateFinishedElementExecutor>>>());
    }
    List<TemplateFinishedElementExecutor> executors = finishedCallbacks
            .get(annotation.getClass());
    if (executors == null) {
      finishedCallbacks.put(annotation.getClass(),
              new ArrayList<TemplateFinishedElementExecutor>());
    }
    beanExecutorMapping.get(beanInst).add(Tuple.of(element, executors));
    elementAnnotation.put(element, annotation);
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
