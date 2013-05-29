package org.jboss.errai.ui.shared.wrapper;

import com.google.gwt.dom.client.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Read only wrapper to transform a gwt dom nodeList to w3c one.
 * @author edewit@redhat.com
 */
public class NodeListWrapper implements NodeList {
  private final com.google.gwt.dom.client.NodeList<com.google.gwt.dom.client.Node> nodeList;

  public NodeListWrapper(com.google.gwt.dom.client.NodeList<com.google.gwt.dom.client.Node> nodeList) {
    this.nodeList = nodeList;
  }

  public Node item(int index) {
    final com.google.gwt.dom.client.Node node = nodeList.getItem(index);
    if (node instanceof Element) {
      return new ElementWrapper((Element) node);
    }
    return new NodeWrapper(node);
  }

  @Override
  public int getLength() {
    return nodeList.getLength();
  }
}
