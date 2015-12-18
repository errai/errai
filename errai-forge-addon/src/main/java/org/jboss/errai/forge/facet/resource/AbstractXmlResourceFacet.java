/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.resource;

import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.errai.forge.xml.ElementFactory;
import org.jboss.errai.forge.xml.XmlParser;
import org.jboss.errai.forge.xml.XmlParserFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

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
  protected final XPathFactory xPathFactory = XPathFactory.newInstance();
  
  @Inject
  protected XmlParserFactory xmlParserFactory;
  
  @Override
  public boolean install() {
    try {
      final File file = getResFile(getRelPath());

      final XmlParser xmlParser = xmlParserFactory.newXmlParser(file);

      final XPath xPath = xPathFactory.newXPath();
      final Map<XPathExpression, Collection<Node>> toInsert = getElementsToInsert(xPath, xmlParser);
      final Map<XPathExpression, Node> replacements = getReplacements(xPath, xmlParser);

      for (final Entry<XPathExpression, Collection<Node>> entry : toInsert.entrySet()) {
        xmlParser.addChildNodes(entry.getKey(), entry.getValue());
      }

      for (final Entry<XPathExpression, Node> entry : replacements.entrySet()) {
        xmlParser.replaceNode(entry.getKey(), entry.getValue());
      }

      xmlParser.close();
    }
    catch (Exception e) {
      error("Error: failed to add required inheritance to module.", e);
      return false;
    }

    return true;
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
      final XmlParser xmlParser = xmlParserFactory.newXmlParser(file);
      final XPath xPath = xPathFactory.newXPath();
      final Map<XPathExpression, Collection<Node>> insertedToCheck = getElementsToVerify(xPath, xmlParser);
      final Map<XPathExpression, Node> replacedToCheck = getReplacements(xPath, xmlParser);

      for (final XPathExpression expression : insertedToCheck.keySet()) {
        if (xmlParser.hasNode(expression)) {
          for (final Node inserted : insertedToCheck.get(expression)) {
            if (!xmlParser.hasMatchingChild(expression, inserted)) {
              return false;
            }
          }
        }
        else {
          return false;
        }
      }

      for (final XPathExpression expression : replacedToCheck.keySet()) {
        if (!xmlParser.matches(expression, replacedToCheck.get(expression))) {
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
   * Get a map of parent nodes to xml fragments used to verify the installation
   * of this facet. Nodes in collections in the values of this map must match
   * children of the node matching the xpath expression key if this facet is
   * installed. If a subclass does not override this, the default value will be
   * the map returned by
   * {@link AbstractXmlResourceFacet#getElementsToInsert(Document)
   * getElementsToInsert}.
   */
  protected Map<XPathExpression, Collection<Node>> getElementsToVerify(final XPath xPath,
          final ElementFactory elemFactory) throws ParserConfigurationException, XPathExpressionException {
    return getElementsToInsert(xPath, elemFactory);
  }

  @Override
  public boolean uninstall() {
    final File file = getResFile(getRelPath());
    if (!file.exists())
      // XXX not sure if this case should return true or false...
      return true;

    try {
      final XmlParser xmlParser = xmlParserFactory.newXmlParser(file);
      final XPath xPath = xPathFactory.newXPath();
      final Map<XPathExpression, Collection<Node>> insertedNodes = getElementsToInsert(xPath, xmlParser);
      final Map<XPathExpression, Node> replacedNodes = getRemovalMap(xPath, xmlParser);

      for (final Entry<XPathExpression, Collection<Node>> entry : insertedNodes.entrySet()) {
        if (xmlParser.hasNode(entry.getKey())) {
          for (final Node inserted : entry.getValue()) {
            xmlParser.removeChildNode(entry.getKey(), inserted);
          }
        }
      }

      for (final Entry<XPathExpression, Node> entry : replacedNodes.entrySet()) {
        if (xmlParser.hasNode(entry.getKey())) {
          xmlParser.replaceNode(entry.getKey(), entry.getValue());
        }
      }
      
      xmlParser.close();
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
   * @param elemFactory
   *          Used to generate {@link Node Nodes}.
   * @return A map of xpath expressions to nodes, used to uninstall this facet.
   */
  protected abstract Map<XPathExpression, Node> getRemovalMap(final XPath xPath, final ElementFactory elemFactory)
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
   * @param elemFactory
   *          The returned nodes should be created with this.
   * @param xPath
   *          Used to generate xpath expressions for finding nodes to add
   *          children to.
   * @return A map of {@link XPathExpression XPathExpressions} (for finding
   *         parent nodes), to collections of {@link Node Nodes} to add as
   *         children.
   */
  protected abstract Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath,
          final ElementFactory elemFactory) throws ParserConfigurationException, XPathExpressionException;

  /**
   * Get a Map of nodes to be replaced in an XML configuration file.
   * 
   * @param elemFactory
   *          The returned nodes should be created with this.
   * @param xPath
   *          Used to generate xpath expressions for finding nodes to replace.
   * @return A map of {@link XPathExpression XPathExpressions} (for finding
   *         nodes to replace), to replacement {@link Node Nodes} to add as
   *         children.
   */
  protected abstract Map<XPathExpression, Node> getReplacements(final XPath xPath, final ElementFactory elemFactory)
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

}
