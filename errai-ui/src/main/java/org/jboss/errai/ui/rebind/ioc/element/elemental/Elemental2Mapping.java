package org.jboss.errai.ui.rebind.ioc.element.elemental;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Elemental2Mapping {

  private static final Map<Class<?>, String[]> tagNamesByDomInterfaces = new HashMap<>();

  static {
    put(HTMLDivElement.class, "div");
    put(HTMLButtonElement.class, "button");
    put(HTMLHeadingElement.class, "h1", "h2", "h3", "h4", "h5", "h6");
  }

  private static <T extends HTMLElement> void put(final Class<T> domInterface, String... tags) {
    tagNamesByDomInterfaces.put(domInterface, tags);
  }

  public static Optional<String[]> tagsFor(Class<?> domInterface) {
    Optional<String[]> strings = Optional.ofNullable(tagNamesByDomInterfaces.get(domInterface));

    System.out.println(domInterface);
    System.out.println(strings);
    return strings;
  }

}
