package org.jboss.errai.ui.shared;

import org.w3c.dom.Element;

/**
 * A simple dom visitor interface.
 */
public interface DomVisitor {
  /**
   * Visits an element in the dom, returns true if the visitor should
   * continue visiting down the dom.
   * @param element the root element to visit
   */
  boolean visit(Element element);
}
