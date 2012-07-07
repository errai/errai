package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;

public interface Visitor<T> {
  void visit(VisitContextMutable<T> context, Element element);
}
