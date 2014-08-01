package org.jboss.errai.ui.shared.api.style;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Element;

/**
 * @author Mike Brock
 */
public class StyleBindingsRegistry {
  private static StyleBindingsRegistry INSTANCE = new StyleBindingsRegistry();

  private final Map<Object, List<Object>> houseKeepingMap = new HashMap<Object, List<Object>>();
  private final Map<Class<? extends Annotation>, List<StyleBindingExecutor>> styleBindings =
      new HashMap<Class<? extends Annotation>, List<StyleBindingExecutor>>();

  private final Map<Annotation, List<ElementBinding>> elementBindings = new HashMap<Annotation, List<ElementBinding>>();
  private final Map<Class<? extends Annotation>, Set<Annotation>> mapping =
      new HashMap<Class<? extends Annotation>, Set<Annotation>>();

  public void addStyleBinding(final Object beanInst, final Class<? extends Annotation> annotation,
          final StyleBindingExecutor binding) {

    List<StyleBindingExecutor> styleBindingList = styleBindings.get(annotation);
    if (styleBindingList == null) {
      styleBindings.put(annotation, styleBindingList = new ArrayList<StyleBindingExecutor>());
    }
    styleBindingList.add(binding);
    recordHouskeepingData(beanInst, binding);
    updateStyles();
  }

  public void addElementBinding(final Object beanInst, final Annotation annotation, final Element element) {
    addElementBinding(annotation, new ElementBinding(this, element, beanInst));
  }

  private void addElementBinding(final Annotation annotation, final ElementBinding binding) {
    List<ElementBinding> elementsList = elementBindings.get(annotation);
    if (elementsList == null) {
      elementBindings.put(annotation, elementsList = new ArrayList<ElementBinding>());
    }
    elementsList.add(binding);
    Set<Annotation> mappedAnnotations = mapping.get(annotation.annotationType());
    if (mappedAnnotations == null) {
      mapping.put(annotation.annotationType(), mappedAnnotations = new HashSet<Annotation>());
    }
    mappedAnnotations.add(annotation);
    recordHouskeepingData(binding.getBeanInstance(), binding);
  }

  private void recordHouskeepingData(final Object beanInst, final Object toClean) {
    List<Object> toCleanList = houseKeepingMap.get(beanInst);
    if (toCleanList == null) {
      houseKeepingMap.put(beanInst, toCleanList = new ArrayList<Object>());
    }
    toCleanList.add(toClean);
  }

  public void cleanAllForBean(final Object beanInst) {
    final List<Object> toCleanList = houseKeepingMap.get(beanInst);
    if (toCleanList != null) {
      for (final Object o : toCleanList) {
        for (final List<StyleBindingExecutor> executorList : styleBindings.values()) {
          executorList.remove(o);
        }
        for (final List<ElementBinding> bindingList : elementBindings.values()) {
          bindingList.remove(o);
        }
      }

      houseKeepingMap.remove(beanInst);
    }
  }

  public void updateStyles() {
    updateStyles(null);
  }

  public void updateStyles(Object beanInst) {
    for (final Map.Entry<Class<? extends Annotation>, List<StyleBindingExecutor>> entry : styleBindings.entrySet()) {
      if (mapping.containsKey(entry.getKey())) {
        for (final Annotation mappedAnnotation : mapping.get(entry.getKey())) {
          final List<ElementBinding> elementList = elementBindings.get(mappedAnnotation);
          if (elementList != null) {
            for (final ElementBinding binding : elementList) {
              if (beanInst == null || beanInst.equals(binding.getBeanInstance())) {
                for (final StyleBindingExecutor executor : entry.getValue()) {
                  if (executor instanceof AnnotationStyleBindingExecutor) {
                    ((AnnotationStyleBindingExecutor) executor).invokeBinding(binding.getElement(), mappedAnnotation);
                  }
                  else {
                    executor.invokeBinding(binding.getElement());
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  public static StyleBindingsRegistry get() {
    return INSTANCE;
  }

  public static void set(StyleBindingsRegistry registry) {
    INSTANCE = registry;
  }

  public static void reset() {
    if (INSTANCE != null) {
      INSTANCE.elementBindings.clear();
      INSTANCE.houseKeepingMap.clear();
      INSTANCE.mapping.clear();
      INSTANCE.styleBindings.clear();
    }

    set(new StyleBindingsRegistry());
  }
}
