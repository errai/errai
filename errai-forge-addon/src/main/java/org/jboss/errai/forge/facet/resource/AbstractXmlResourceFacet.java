package org.jboss.errai.forge.facet.resource;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A base class for modifying XML-based configuration files. Concrete subclasses
 * may modify the field {@link AbstractXmlResourceFacet#xmlProperties
 * xmlProperties}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractXmlResourceFacet extends AbstractBaseFacet {

  /**
   * The properties used when writing to an XML file.
   * 
   * @see {@link OutputKeys}, {@link Transformer}
   */
  final protected Properties xmlProperties = new Properties();
  protected final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
  protected final XPathFactory xPathFactory = XPathFactory.newInstance();
  protected final TransformerFactory transFactory = TransformerFactory.newInstance();

  public AbstractXmlResourceFacet() {
    xmlProperties.setProperty(OutputKeys.INDENT, "yes");
  }

  @Override
  public boolean install() {
    try {
      final File file = getResFile(getRelPath());
      if (!file.exists()) {
        throw new IllegalStateException(String.format("The given xml file %s does not exist.", file.getAbsolutePath()));
      }

      final DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
      final Document doc = builder.parse(file);
      final XPath xPath = xPathFactory.newXPath();
      final Map<XPathExpression, Collection<Node>> toInsert = getElementsToInsert(xPath, doc);
      final Map<XPathExpression, Node> replacements = getReplacements(xPath, doc);

      for (final XPathExpression expression : toInsert.keySet()) {
        final Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
          for (final Node newNode : toInsert.get(expression)) {
            node.appendChild(newNode);
          }
        }
      }

      for (final XPathExpression expression : replacements.keySet()) {
        final Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
        if (node != null) {
          final Node parent = node.getParentNode();
          final Node newNode = replacements.get(expression);
          parent.replaceChild(newNode, node);
        }
      }

      writeDocument(doc, file);
    }
    catch (Exception e) {
      error("Error: failed to add required inheritance to module.", e);
      return false;
    }

    return true;
  }
  
  protected void writeDocument(final Document doc, final File file) throws TransformerException {
      final Transformer transformer = transFactory.newTransformer();
      final DOMSource source = new DOMSource(doc);
      final StreamResult res = new StreamResult(file);
      transformer.setOutputProperties(xmlProperties);
      transformer.transform(source, res);
  }

  @Override
  public boolean isInstalled() {
    final String relPath = getRelPath();
    if (relPath == null)
      // Project config has not been setup yet.
      return false;

    final File file = getResFile(relPath);
    if (!file.exists())
      return false;

    try {
      final DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
      final Document doc = builder.parse(file);
      final XPath xPath = xPathFactory.newXPath();
      final Map<XPathExpression, Collection<Node>> insertedToCheck = getElementsToVerify(xPath, doc);
      final Map<XPathExpression, Node> replacedToCheck = getReplacements(xPath, doc);

      for (final XPathExpression expression : insertedToCheck.keySet()) {
        final Node parent = (Node) expression.evaluate(doc, XPathConstants.NODE);
        if (parent != null) {
          for (final Node inserted : insertedToCheck.get(expression)) {
            if (!hasMatchingChild(parent, inserted)) {
              return false;
            }
          }
        }
        else {
          return false;
        }
      }

      for (final XPathExpression expression : replacedToCheck.keySet()) {
        final Node replaced = (Node) expression.evaluate(doc, XPathConstants.NODE);
        if (replaced == null || !matches(replacedToCheck.get(expression), replaced)) {
          return false;
        }
      }

    }
    catch (Exception e) {
      error("Error occurred while attempting to verify xml resource " + file.getAbsolutePath(), e);
      return false;
    }

    return true;
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
  protected Node getMatchingChild(final Node parent, final Node inserted) {
    for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
      if (matches(inserted, parent.getChildNodes().item(i)))
        return parent.getChildNodes().item(i);
    }

    return null;
  }

  /**
   * Check if a matching child {@link Node} from a given parent node exists.
   * 
   * @param parent
   *          The parent node of the children to be searched.
   * @param inserted
   *          The node to be matched against.
   * @return Returns true if there exists a node, {@code result}, such that
   *         {@code result.getParentNode().isSameNode(parent) && matches(inserted, result)}
   *         .
   */
  protected boolean hasMatchingChild(final Node parent, final Node inserted) {
    return getMatchingChild(parent, inserted) != null;
  }

  /**
   * Get a map of parent nodes to xml fragments used to verify the installation
   * of this facet. Nodes in collections in the values of this map must match
   * children of the node matching the xpath expression key if this facet is
   * installed. If a subclass does not override this, the default value will be
   * the map returned by
   * {@link AbstractXmlResourceFacet#getElementsToInsert(Document)
   * getElementsToInsert}.
   */
  protected Map<XPathExpression, Collection<Node>> getElementsToVerify(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException {
    return getElementsToInsert(xPath, doc);
  }

  @Override
  public boolean uninstall() {
    final File file = getResFile(getRelPath());
    if (!file.exists())
      // XXX not sure if this case should return true or false...
      return true;

    try {
      final DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
      final Document doc = builder.parse(file);
      final XPath xPath = xPathFactory.newXPath();
      final Map<XPathExpression, Collection<Node>> insertedNodes = getElementsToInsert(xPath, doc);
      final Map<XPathExpression, Node> replacedNodes = getRemovalMap(xPath, doc);

      for (final XPathExpression expression : insertedNodes.keySet()) {
        final Node parent = (Node) expression.evaluate(doc, XPathConstants.NODE);
        if (parent != null) {
          for (final Node inserted : insertedNodes.get(expression)) {
            final Node match = getMatchingChild(parent, inserted);
            if (match != null) {
              parent.removeChild(match);
            }
          }
        }
      }

      for (final XPathExpression expression : replacedNodes.keySet()) {
        final Node replaced = (Node) expression.evaluate(doc, XPathConstants.NODE);
        if (replaced != null) {
          final Node parent = replaced.getParentNode();
          final Node newNode = replacedNodes.get(expression);
          parent.replaceChild(newNode, replaced);
        }
      }
      
      writeDocument(doc, file);
    }
    catch (Exception e) {
      error("Error occurred while attempting to verify xml resource " + file.getAbsolutePath(), e);
      return false;
    }

    return true;
  }

  /**
   * Return a map of xpath expressions of nodes to be replaced to their
   * replacements, used when this facet is uninstalled.
   * 
   * @param xPath
   *          Used to generate {@link XPathExpression XPathExpressions}.
   * @param doc
   *          Used to generate or import {@link Node Nodes}.
   * @return A map of xpath expressions to nodes, used to uninstall this facet.
   */
  protected abstract Map<XPathExpression, Node> getRemovalMap(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException;

  protected File getResFile(final String relPath) {
    File file = new File(relPath);
    if (!file.isAbsolute())
      file = new File(getProject().getRootDirectory().getUnderlyingResourceObject(), file.getPath());

    return file;
  }

  /**
   * Get DOM nodes to write to an XML configuration file. Concrete subclasses
   * should return a map with keys as parent nodes and values as collections of
   * nodes that will be merged as children of the the nodes matched by the
   * respective key values. Each key should only match a unique node (i.e. this
   * is a one-to-many map).
   * 
   * @param doc
   *          The returned nodes should be created with or imported into this
   *          document.
   * @param xPath
   *          Used to generate xpath expressions for finding nodes to add
   *          children to.
   * @return A map of {@link XPathExpression XPathExpressions} (for finding
   *         parent nodes), to collections of {@link Node Nodes} to add as
   *         children.
   */
  protected abstract Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException;

  /**
   * Get a Map of nodes to be replaced in an XML configuration file.
   * 
   * @param doc
   *          The document to which all returned nodes should belong.
   * @param xPath
   *          Used to generate xpath expressions for finding nodes to replace.
   * @return A map of {@link XPathExpression XPathExpressions} (for finding
   *         nodes to replace), to replacement {@link Node Nodes} to add as
   *         children.
   */
  protected abstract Map<XPathExpression, Node> getReplacements(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException;

  /**
   * Get the relative path of XML file to be configured by this facet. Concrete
   * subclasses must return the path (relative to the project root directory) of
   * the XML file they are configuring.
   * 
   * @return The path (relative to the project root directory) of an XML
   *         configuration file.
   */
  protected abstract String getRelPath();

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
  protected boolean matches(Node node, Node other) {
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
}