package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;

public class Visit {
  public static <T> VisitContext<T> breadthFirst(Element root, Visitor<T> visitor) {
    return breadthFirst(new VisitContextImpl<T>(), root, visitor);
  }

  private static <T> VisitContext<T> breadthFirst(VisitContextImpl<T> context, Element root, Visitor<T> visitor) {
    if (root == null)
      throw new IllegalArgumentException("Root Element to visit must not be null.");

    if (context == null)
      context = new VisitContextImpl<T>();

    Element current = root;
    visitor.visit(context, current);
    current = current.getFirstChildElement();

    while (current != null && !context.isVisitComplete()) {
      breadthFirst(context, current, visitor);
      current = current.getNextSiblingElement();
    }

    return context;
  }

}
