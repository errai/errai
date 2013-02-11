package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class StyleBindingsRegistry {
  private static final StyleBindingsRegistry INSTANCE = new StyleBindingsRegistry();

  private StyleBindingsRegistry() {
  }

  private final Map<Class<? extends Annotation>, List<StyleBindingExecutor>> styleBindings
      = new HashMap<Class<? extends Annotation>, List<StyleBindingExecutor>>();

  private final Map<Class<? extends Annotation>, List<ElementBinding>> elementBindings
      = new HashMap<Class<? extends Annotation>, List<ElementBinding>>();

  public void addStyleBinding(final Class annotation, final StyleBindingExecutor binding) {
    List<StyleBindingExecutor> styleBindingList = styleBindings.get(annotation);
    if (styleBindingList == null) {
      styleBindings.put(annotation, styleBindingList = new ArrayList<StyleBindingExecutor>());
    }
    styleBindingList.add(binding);

    updateStyles();
  }

  public void addElementBinding(final Class annotation, final Element element) {
    addElementBinding(annotation, new ElementBinding(this, element));
  }

  private void addElementBinding(final Class annotation, final ElementBinding element) {
    List<ElementBinding> elementsList = elementBindings.get(annotation);
    if (elementsList == null) {
      elementBindings.put(annotation, elementsList = new ArrayList<ElementBinding>());
    }
    elementsList.add(element);
  }

  public void removeStyleBinding(final StyleBindingExecutor binding) {
    for (List<StyleBindingExecutor> bindingList : styleBindings.values()) {
      bindingList.remove(binding);
    }
  }

  public void removeElementBinding(final ElementBinding element) {
    for (List<ElementBinding> bindingList : elementBindings.values()) {
       bindingList.remove(element);
    }
    element.clean();
  }

  public void updateStyles() {
    for (Map.Entry<Class<? extends Annotation>, List<StyleBindingExecutor>> entry : styleBindings.entrySet()) {
      final List<ElementBinding> elementList = elementBindings.get(entry.getKey());
      if (elementList != null) {
        for (final ElementBinding element : elementList) {
          for (StyleBindingExecutor executor : entry.getValue()) {
            executor.invokeBinding(element.getElement());
          }
        }
      }
    }
  }

  public static StyleBindingsRegistry get() {
    return INSTANCE;
  }
}
