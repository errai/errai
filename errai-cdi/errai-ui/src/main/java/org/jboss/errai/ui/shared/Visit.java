package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;

public class Visit {
  public static void accept(Element root, Visitor visitor) {
    if (root == null)
      throw new IllegalArgumentException("Root Element to visit must not be null.");

    VisitContextImpl context = new VisitContextImpl();

    Element current = root;
    visitor.visit(context, current);
    current = current.getFirstChildElement();

    while (current != null && !context.isVisitComplete()) {
      accept(current, visitor);
      current = current.getNextSiblingElement();
    }
  }
}
