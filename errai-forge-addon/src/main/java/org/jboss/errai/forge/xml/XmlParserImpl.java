/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.forge.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class XmlParserImpl implements AutoCloseable, XmlParser {

  private final File xmlFile;
  private final DocumentBuilder docBuilder;
  private final Transformer transformer;
  
  private final Map<Element, Map<String, String>> openAttributeMaps = new HashMap<Element, Map<String,String>>();
  
  private Document document;
  
  private boolean modified;

  public XmlParserImpl(final File xmlFile, final Properties xmlProperties, final DocumentBuilder docBuilder,
          final Transformer transformer) {
    this.xmlFile = xmlFile;
    this.docBuilder = docBuilder;
    this.transformer = transformer;
    
    transformer.setOutputProperties(xmlProperties);
  }
  
  /* (non-Javadoc)
   * @see org.jboss.errai.forge.xml.XmlParser#open()
   */
  @Override
  public void open() throws SAXException, IOException {
    if (!isOpen()) {
      document = docBuilder.parse(xmlFile);
    } else {
      throw new IllegalStateException("This instance has already been opened.");
    }
  }
  
  /* (non-Javadoc)
   * @see org.jboss.errai.forge.xml.XmlParser#close()
   */
  @Override
  public void close() throws TransformerException {
    if (isOpen()) {
      if (modified)
        flush();
      document = null;
      openAttributeMaps.clear();
    }
  }

  /* (non-Javadoc)
   * @see org.jboss.errai.forge.xml.XmlParser#flush()
   */
  @Override
  public void flush() throws TransformerException {
    assertOpen();
    transformer.transform(new DOMSource(document), new StreamResult(xmlFile));
    modified = false;
  }

  private void assertOpen() {
    if (!isOpen()) {
      throw new IllegalStateException("Document must be open for this operation.");
    }
  }

  /* (non-Javadoc)
   * @see org.jboss.errai.forge.xml.XmlParser#isOpen()
   */
  @Override
  public boolean isOpen() {
    return document != null;
  }

  /* (non-Javadoc)
   * @see org.jboss.errai.forge.xml.XmlParser#addChildNodesByXPath(javax.xml.xpath.XPathExpression, java.util.Collection)
   */
  @Override
  public boolean addChildNodes(final XPathExpression expression, final Collection<Node> nodes)
          throws XPathExpressionException {
    assertOpen();

    final Node node = getNodeByXPath(expression);
    if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
      for (final Node newNode : nodes) {
        node.appendChild(newNode);
      }
      return (modified = true);
    }
    
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.jboss.errai.forge.xml.XmlParser#replaceNodesByXPath(javax.xml.xpath.XPathExpression, org.w3c.dom.Node)
   */
  @Override
  public boolean replaceNode(final XPathExpression expression, final Node replacement)
          throws XPathExpressionException {
    assertOpen();

    final Node node = getNodeByXPath(expression);
    if (node != null) {
      final Node parent = node.getParentNode();
      parent.replaceChild(replacement, node);
      
      return (modified = true);
    }
    
    return false;
  }

  @Override
  public Element createElement(final String tagName) {
    assertOpen();

    return document.createElement(tagName);
  }

  @Override
  public Element importElement(final Element element, boolean deep) {
    return (Element) document.importNode(element, deep);
  }

  private Node getNodeByXPath(XPathExpression expression) throws XPathExpressionException {
    return (Node) expression.evaluate(document, XPathConstants.NODE);
  }

  private boolean hasMatchingChild(Node parent, Node child) {
    return getMatchingChild(parent, child) != null;
  }

  /**
   * Find a matching child {@link Node} from a given parent node.
   * 
   * @param parent
   *          The parent node of the children to be searched.
   * @param inserted
   *          The node to be matched against.
   * @return Returns a node, {@code result}, such that
   *         {@code result.getParentNode().isSameNode(parent) && matches(inserted, result)}
   *         .
   */
  private Node getMatchingChild(Node parent, Node child) {
    for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
      if (matches(child, parent.getChildNodes().item(i)))
        return parent.getChildNodes().item(i);
    }

    return null;
  }

  /**
   * Check if a node is consistent with another. A node, {@code other} is
   * consistent with another node, {@code node}, if the tree rooted at
   * {@code node} is a subtree of {@code other} (i.e. every child element,
   * attribute, or text value in {@code node} exists in the same relative path
   * in {@code other}).
   * 
   * @param node
   *          The primary node for matching against.
   * @param other
   *          The secondary node being matched against the primary node.
   * @return True iff {@code other} is consistent with {@code node}.
   */
  private boolean matches(Node node, Node other) {
    if (node.getNodeType() == Node.TEXT_NODE) {
      return other.getNodeType() == Node.TEXT_NODE && node.getNodeValue().equals(other.getNodeValue());
    }

    if (!(other instanceof Element) || !(node instanceof Element)) {
      return false;
    }

    final Element e1 = (Element) node, e2 = (Element) other;
    if (!e1.getNodeName().equals(e2.getNodeName()))
      return false;

    // other must have attributes consistent with node
    final NamedNodeMap attributes = e1.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      final Node item = attributes.item(i);
      if (!e2.hasAttribute(item.getNodeName()) || !e2.getAttribute(item.getNodeName()).equals(item.getNodeValue()))
        return false;
    }

    // children of other must be consistent with children of node
    if (e1.hasChildNodes()) {
      outer: for (Node child = e1.getFirstChild(); child != null; child = child.getNextSibling()) {
        if (child.getNodeType() == Node.ELEMENT_NODE || child.getNodeType() == Node.TEXT_NODE) {
          for (Node otherChild = e2.getFirstChild(); otherChild != null; otherChild = otherChild.getNextSibling()) {
            if (otherChild.getNodeType() == child.getNodeType() && matches(child, otherChild))
              continue outer;
          }
        }
        else {
          continue;
        }

        return false;
      }
    }

    return true;
  }

  @Override
  public boolean hasMatchingChild(final XPathExpression parentExpression, final Node child)
          throws XPathExpressionException {
    assertOpen();

    final Node parentNode = getNodeByXPath(parentExpression);
    
    return parentNode != null && hasMatchingChild(parentNode, child);
  }

  @Override
  public boolean hasNode(final XPathExpression expression) throws XPathExpressionException {
    assertOpen();

    return getNodeByXPath(expression) != null;
  }

  @Override
  public boolean matches(final XPathExpression expression, final Node node) throws XPathExpressionException {
    assertOpen();

    final Node documentNode = getNodeByXPath(expression);

    return documentNode != null && matches(node, documentNode);
  }

  @Override
  public boolean removeNode(final XPathExpression expression) throws XPathExpressionException {
    assertOpen();

    final Node removableNode = getNodeByXPath(expression);
    if (removableNode == null)
      return false;
    
    removableNode.getParentNode().removeChild(removableNode);

    return (modified = true);
  }

  @Override
  public boolean removeChildNode(final XPathExpression parentExpression, final Node child) throws XPathExpressionException {
    assertOpen();
    
    final Node parent = getNodeByXPath(parentExpression);
    if (parent == null)
      return false;
    
    final Node matchingChild = getMatchingChild(parent, child);
    if (matchingChild == null)
      return false;
    
    parent.removeChild(matchingChild);

    return (modified = true);
  }

  @Override
  public Map<String, String> getAttributes(final XPathExpression elementExpression) throws XPathExpressionException {
    final Element element = (Element) getNodeByXPath(elementExpression);
    if (element != null) {
      final Map<String, String> retVal = new Map<String, String>() {

        @Override
        public String put(final String key, final String value) {
          assertOpen();
          modified = true;

          final String prev = element.getAttribute(key);
          element.setAttribute(key, value);

          return (!prev.equals("")) ? prev : null;
        }

        @Override
        public void putAll(final Map<? extends String, ? extends String> m) {
          assertOpen();
          modified = true;
          for (final Entry<? extends String, ? extends String> pair : m.entrySet()) {
            element.setAttribute(pair.getKey(), pair.getValue());
          }
        }

        @Override
        public String remove(Object key) {
          assertOpen();
          modified = true;

          final String prev = element.getAttribute((String) key);
          element.removeAttribute((String) key);
          
          return (!prev.equals("")) ? prev : null;
        }

        @Override
        public void clear() {
          throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
          return element.hasAttribute((String) key);
        }

        @Override
        public boolean containsValue(Object value) {
          throw new UnsupportedOperationException();
        }

        @Override
        public String get(Object key) {
          return element.getAttribute((String) key);
        }

        @Override
        public Set<String> keySet() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> values() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Set<java.util.Map.Entry<String, String>> entrySet() {
          throw new UnsupportedOperationException();
        }

      };

      
      return retVal;
    }
    
    return null;
  }

}
