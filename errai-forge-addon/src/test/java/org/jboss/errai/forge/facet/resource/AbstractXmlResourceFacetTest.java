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

import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.errai.forge.xml.ElementFactory;
import org.jboss.errai.forge.xml.XmlParserFactory;
import org.jboss.forge.addon.projects.Project;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class AbstractXmlResourceFacetTest extends ForgeTest {
  
  @Inject
  private XmlParserFactory xmlParserFactory;

  public class TestXmlResourceFacet extends AbstractXmlResourceFacet {
    private final String relPath;
    private final Map<String, Collection<Node>> nodes;
    private final Map<String, Node> replacements;
    private final Map<String, Node> replacementsRemoval;

    public TestXmlResourceFacet(final String relPath, final Map<String, Collection<Node>> nodes,
            final Map<String, Node> replacements, final Map<String, Node> replacementsRemoval) {
      this.relPath = relPath;
      this.nodes = nodes;
      this.replacements = replacements;
      this.replacementsRemoval = replacementsRemoval;
      this.xmlParserFactory = AbstractXmlResourceFacetTest.this.xmlParserFactory;
    }

    @Override
    protected Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath,
            final ElementFactory elemFactory) throws ParserConfigurationException, XPathExpressionException {
      final Map<XPathExpression, Collection<Node>> retVal = new HashMap<XPathExpression, Collection<Node>>(nodes.size());

      for (final String rawExpression : nodes.keySet()) {
        final Collection<Node> value = new ArrayList<Node>(nodes.get(rawExpression).size());
        for (final Node importNode : nodes.get(rawExpression)) {
          value.add(elemFactory.importElement((Element) importNode, true));
        }
        retVal.put(xPath.compile(rawExpression), value);
      }

      return retVal;
    }

    private Map<XPathExpression, Node> prepMap(final XPath xPath, final ElementFactory elemFactory,
            final Map<String, Node> map) throws XPathExpressionException, DOMException {
      final Map<XPathExpression, Node> retVal = new HashMap<XPathExpression, Node>(map.size());

      for (final String rawExpression : map.keySet()) {
        retVal.put(xPath.compile(rawExpression), elemFactory.importElement((Element) map.get(rawExpression), true));
      }

      return retVal;
    }

    @Override
    protected Map<XPathExpression, Node> getReplacements(final XPath xPath, final ElementFactory elemFactory)
            throws ParserConfigurationException, XPathExpressionException, DOMException {
      return prepMap(xPath, elemFactory, replacements);
    }

    @Override
    protected String getRelPath() {
      return relPath;
    }

    @Override
    protected Map<XPathExpression, Node> getRemovalMap(final XPath xPath, final ElementFactory elemFactory)
            throws ParserConfigurationException, XPathExpressionException {
      return prepMap(xPath, elemFactory, replacementsRemoval);
    }
  }

  @Test
  public void testIsInstalledRecursiveCase() throws Exception {
    final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    final List<Node> nodes = Arrays.asList(new Node[] { doc.createElement("first"), doc.createElement("first") });
    nodes.get(0).appendChild(doc.createElement("second")).appendChild(doc.createElement("third"));
    nodes.get(0).getFirstChild().appendChild(doc.createElement("fourth"));
    nodes.get(0).appendChild(doc.createElement("fifth"));
    ((Element) nodes.get(1).appendChild(doc.createElement("second"))).setAttribute("name", "test");

    final Map<String, Collection<Node>> insertMap = new HashMap<String, Collection<Node>>(1);
    insertMap.put("/main", nodes);

    final Map<String, Node> empty = new HashMap<String, Node>(0);

    final Project project = initializeJavaProject();
    final TestXmlResourceFacet testFacet = new TestXmlResourceFacet(
            writeResourceToFile("org/jboss/errai/forge/facet/resource/AbstractXmlResourceFacetTest-1.xml"), insertMap, empty, empty);
    testFacet.setFaceted(project);

    assertTrue(testFacet.isInstalled());
  }

  @Test
  public void testNotInstalledRecursiveCase() throws Exception {
    final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    final List<Node> nodes = Arrays.asList(new Node[] { doc.createElement("first"), doc.createElement("first") });
    nodes.get(0).appendChild(doc.createElement("second")).appendChild(doc.createElement("third"));
    // This extra attribute should cause isInstalled to return false.
    ((Element) nodes.get(0).getFirstChild()).setAttribute("foo", "bar");
    nodes.get(0).getFirstChild().appendChild(doc.createElement("fourth"));
    nodes.get(0).appendChild(doc.createElement("fifth"));
    ((Element) nodes.get(1).appendChild(doc.createElement("second"))).setAttribute("name", "test");

    final Map<String, Collection<Node>> insertMap = new HashMap<String, Collection<Node>>(1);
    insertMap.put("/main", nodes);

    final Map<String, Node> empty = new HashMap<String, Node>(0);

    final Project project = initializeJavaProject();
    final TestXmlResourceFacet testFacet = new TestXmlResourceFacet(
            writeResourceToFile("org/jboss/errai/forge/facet/resource/AbstractXmlResourceFacetTest-1.xml"), insertMap, empty, empty);
    testFacet.setFaceted(project);

    assertFalse(testFacet.isInstalled());
  }

  @Test
  public void testSimpleReplacement() throws Exception {
    final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

    final Map<String, Node> replacements = new HashMap<String, Node>(1);
    final String key = "/main/first/second[@name='test']/..";
    final Element value = doc.createElement("different");
    replacements.put(key, value);

    final Project project = initializeJavaProject();
    final TestXmlResourceFacet testFacet = new TestXmlResourceFacet(
            writeResourceToFile("org/jboss/errai/forge/facet/resource/AbstractXmlResourceFacetTest-1.xml"), new HashMap<String, Collection<Node>>(0),
            replacements, new HashMap<String, Node>(0));
    testFacet.setFaceted(project);

    assertTrue(testFacet.install());

    final Document resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new File(testFacet.relPath));

    assertEquals(1, resDoc.getElementsByTagName("different").getLength());
    assertEquals(1, resDoc.getElementsByTagName("first").getLength());
  }

  private String writeResourceToFile(final String res) throws IOException {
    final File file = File.createTempFile(getClass().getSimpleName(), ".xml");
    file.deleteOnExit();

    final InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(res);
    final BufferedInputStream stream = new BufferedInputStream(resourceAsStream);
    final BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));

    final byte[] buf = new byte[256];
    for (int read = stream.read(buf); read != -1; read = stream.read(buf)) {
      writer.write(buf, 0, read);
    }

    writer.close();
    stream.close();

    return file.getAbsolutePath();
  }

}
