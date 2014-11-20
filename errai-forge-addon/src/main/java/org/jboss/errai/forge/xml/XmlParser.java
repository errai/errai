package org.jboss.errai.forge.xml;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public interface XmlParser extends ElementFactory {

  public void open() throws SAXException, IOException;

  public void close() throws TransformerException;

  public void flush() throws TransformerException;

  public boolean isOpen();

  public boolean addChildNodes(XPathExpression expression, Collection<Node> nodes)
          throws XPathExpressionException;

  public boolean replaceNode(XPathExpression expression, Node replacement) throws XPathExpressionException;
  
  public boolean removeNode(XPathExpression expression) throws XPathExpressionException;
  
  public boolean removeChildNode(XPathExpression parentExpression, Node child) throws XPathExpressionException;
  
  public boolean hasNode(XPathExpression expression) throws XPathExpressionException;

  public boolean hasMatchingChild(XPathExpression parentExpression, Node child) throws XPathExpressionException;

  public boolean matches(XPathExpression expression, Node node) throws XPathExpressionException;
  
  public Map<String, String> getAttributes(XPathExpression elementExpression) throws XPathExpressionException;

}