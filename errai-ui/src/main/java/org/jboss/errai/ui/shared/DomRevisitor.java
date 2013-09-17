package org.jboss.errai.ui.shared;

import org.w3c.dom.Element;

/**
 * An interface for visiting DOM nodes and performing some post visiting task.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface DomRevisitor extends DomVisitor {

  /**
   * This method is invoked after this element and all of its children have been visited.
   * 
   * @param element
   *          The element that has previously been visited.
   */
  public void afterVisit(Element element);
}
