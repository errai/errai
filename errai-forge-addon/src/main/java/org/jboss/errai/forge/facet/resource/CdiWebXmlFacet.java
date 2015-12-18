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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This facet sets
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ErraiBusServletConfigFacet.class })
public class CdiWebXmlFacet extends AbstractXmlResourceFacet {
  
  public final String erraiServletExpression = "/web-app/servlet[servlet-name[text()='ErraiServlet']]";
  public final String autoDiscoverParamSubExpression = "/init-param[param-name[text()='auto-discover-services']]";
  public final String autoDiscoverParamExpression = erraiServletExpression + autoDiscoverParamSubExpression;

  @Override
  protected Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath,
          final ElementFactory elemFactory) {
    return new HashMap<XPathExpression, Collection<Node>>(0);
  }

  @Override
  protected Map<XPathExpression, Node> getReplacements(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException, XPathExpressionException {
    final XPathExpression key = xPath.compile(autoDiscoverParamExpression);

    final Element value = elemFactory.createElement("init-param");
    value.appendChild(elemFactory.createElement("param-name")).setTextContent("auto-discover-services");
    value.appendChild(elemFactory.createElement("param-value")).setTextContent("false");

    final Map<XPathExpression, Node> replacements = new HashMap<XPathExpression, Node>(1);
    replacements.put(key, value);

    return replacements;
  }

  @Override
  protected String getRelPath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/web.xml";
  }

  @Override
  protected Map<XPathExpression, Node> getRemovalMap(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException, XPathExpressionException {
    assert xPath.compile(erraiServletExpression).evaluate(erraiServletExpression, XPathConstants.NODE) != null;
    final XPathExpression key = xPath.compile(autoDiscoverParamExpression);

    final Element value = elemFactory.createElement("init-param");
    value.appendChild(elemFactory.createElement("param-name")).setTextContent("auto-discover-services");
    value.appendChild(elemFactory.createElement("param-value")).setTextContent("true");

    final Map<XPathExpression, Node> replacements = new HashMap<XPathExpression, Node>(1);
    replacements.put(key, value);

    return replacements;
  }

}
