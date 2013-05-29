package org.jboss.errai.ui.shared.wrapper;

import org.w3c.dom.*;

/**
 * Read only wrapper to transform a gwt dom element to w3c one.
* @author edewit@redhat.com
*/
public class ElementWrapper extends NodeWrapper implements Element {
  private final com.google.gwt.dom.client.Element element;

  public ElementWrapper(com.google.gwt.dom.client.Element element) {
    super(element);
    this.element = element;
  }

  public String getTagName() {
    return element.getTagName();
  }

  @Override
  public String getAttribute(String name) {
    return element.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, String value) throws DOMException {
    element.setAttribute(name, value);
  }

  @Override
  public void removeAttribute(String name) throws DOMException {
    element.removeAttribute(name);
  }

  @Override
  public Attr getAttributeNode(String name) {
    return null;
  }

  @Override
  public Attr setAttributeNode(Attr newAttr) throws DOMException {
    return null;
  }

  @Override
  public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
    return null;
  }

  @Override
  public NodeList getElementsByTagName(String name) {
    return null;
  }

  @Override
  public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
    return null;
  }

  @Override
  public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
  }

  @Override
  public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
  }

  @Override
  public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
    return null;
  }

  @Override
  public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
    return null;
  }

  @Override
  public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
    return null;
  }

  @Override
  public boolean hasAttribute(String name) {
    return element.hasAttribute(name);
  }

  @Override
  public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
    return false;
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    return null;
  }

  @Override
  public void setIdAttribute(String name, boolean isId) throws DOMException {
  }

  @Override
  public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
  }

  @Override
  public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
  }

  public com.google.gwt.dom.client.Element getElement() {
    return element;
  }
}
