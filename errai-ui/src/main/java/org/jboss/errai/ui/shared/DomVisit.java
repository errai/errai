package org.jboss.errai.ui.shared;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class DomVisit {

  /**
   * Called to traverse and visit the tree of {@link org.w3c.dom.Element}s.
   * @param element the root of the tree to traverse and visit
   * @param visitor the visitor to be called on each of the nodes.
   */
  public static void visit(Element element, DomVisitor visitor) {
    if (!visitor.visit(element))
      return;
    NodeList childNodes = element.getChildNodes();
    for (int idx = 0; idx < childNodes.getLength(); idx++) {
      Node childNode = childNodes.item(idx);
      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        visit((Element) childNode, visitor);
      }
    }
  }

  public static void revisit(Element element, DomRevisitor visitor) {
    if (visitor.visit(element)) {
      NodeList childNodes = element.getChildNodes();
      for (int idx = 0; idx < childNodes.getLength(); idx++) {
        Node childNode = childNodes.item(idx);
        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
          revisit((Element) childNode, visitor);
        }
      }
    }
    visitor.afterVisit(element);
  }

}
