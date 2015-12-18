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

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;
import org.jboss.errai.forge.xml.ElementFactory;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This facet configures the ErraiServlet used by the errai-bus project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ WebXmlFacet.class })
public class ErraiBusServletConfigFacet extends AbstractXmlResourceFacet {

  public static final String webXmlRootExpression = "/web-app";

  @Override
  protected Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException, XPathExpressionException {
    final Element servlet = elemFactory.createElement("servlet");
    servlet.appendChild(elemFactory.createElement("servlet-name")).setTextContent("ErraiServlet");
    servlet.appendChild(elemFactory.createElement("servlet-class")).setTextContent(
            "org.jboss.errai.bus.server.servlet.DefaultBlockingServlet");

    final Node initParam = servlet.appendChild(elemFactory.createElement("init-param"));
    initParam.appendChild(elemFactory.createElement("param-name")).setTextContent("auto-discover-services");
    initParam.appendChild(elemFactory.createElement("param-value")).setTextContent("true");

    servlet.appendChild(elemFactory.createElement("load-on-startup")).setTextContent("1");

    final Element servletMapping = elemFactory.createElement("servlet-mapping");
    servletMapping.appendChild(elemFactory.createElement("servlet-name")).setTextContent("ErraiServlet");
    servletMapping.appendChild(elemFactory.createElement("url-pattern")).setTextContent("*.erraiBus");

    final Map<XPathExpression, Collection<Node>> retVal = new HashMap<XPathExpression, Collection<Node>>(1);
    final Collection<Node> nodes = new ArrayList<Node>(2);

    nodes.add(servlet);
    nodes.add(servletMapping);
    retVal.put(xPath.compile(webXmlRootExpression), nodes);

    return retVal;
  }

  @Override
  protected Map<XPathExpression, Collection<Node>> getElementsToVerify(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException, XPathExpressionException {
    final Map<XPathExpression, Collection<Node>> retVal = getElementsToInsert(xPath, elemFactory);

    /*
     * Remove the param-value for auto-discover-services for the purpose of
     * verifying that this facet is installed. This is so that if CdiWebXmlFacet
     * has been installed (and thus has overwritten this value with "false",
     * this facet will still register as installed.
     */
    outer: for (final Node node : retVal.entrySet().iterator().next().getValue()) {
      if (node.getNodeName().equals("servlet")) {
        final Element servlet = (Element) node;
        final NodeList values = servlet.getElementsByTagName("param-value");
        for (int i = 0; i < values.getLength(); i++) {
          final Node prevSibling = values.item(i).getPreviousSibling();
          if (prevSibling != null && prevSibling.getNodeValue() != null && prevSibling.equals("auto-discover-services")) {
            values.item(i).getParentNode().removeChild(values.item(i));
            break outer;
          }
        }
      }
    }

    return retVal;
  }

  @Override
  protected Map<XPathExpression, Node> getReplacements(final XPath xPath, final ElementFactory elemFactory) {
    return new HashMap<XPathExpression, Node>(0);
  }

  @Override
  protected String getRelPath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/web.xml";
  }

  @Override
  protected Map<XPathExpression, Node> getRemovalMap(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException, XPathExpressionException {
    return new HashMap<XPathExpression, Node>(0);
  }

}
