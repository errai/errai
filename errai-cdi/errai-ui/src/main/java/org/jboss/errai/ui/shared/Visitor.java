package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;

public interface Visitor {
  void visit(VisitContext context, Element element);
}
