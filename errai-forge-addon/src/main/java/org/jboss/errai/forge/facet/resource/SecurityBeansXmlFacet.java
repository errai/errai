/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.forge.facet.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;
import org.jboss.errai.forge.xml.ElementFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SecurityBeansXmlFacet extends AbstractXmlResourceFacet {

  @Override
  protected Map<XPathExpression, Node> getRemovalMap(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException,
          XPathExpressionException {
    return new HashMap<XPathExpression, Node>(0);
  }

  @Override
  protected Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException, XPathExpressionException {
    final Map<XPathExpression, Collection<Node>> retVal = new LinkedHashMap<XPathExpression, Collection<Node>>();
    
    final Element interceptors = elemFactory.createElement("interceptors");

    final Element userInterceptor = elemFactory.createElement("class");
    userInterceptor.setTextContent("org.jboss.errai.security.server.SecurityUserInterceptor");
    final Element roleInterceptor = elemFactory.createElement("class");
    roleInterceptor.setTextContent("org.jboss.errai.security.server.ServerSecurityRoleInterceptor");

    retVal.put(xPath.compile("/beans"), Arrays.<Node>asList(
        interceptors
    ));
    retVal.put(xPath.compile("/beans/interceptors"), Arrays.<Node>asList(
        userInterceptor,
        roleInterceptor
    ));

    return retVal;
  }

  @Override
  protected Map<XPathExpression, Node> getReplacements(final XPath xPath, final ElementFactory elemFactory)
          throws ParserConfigurationException,
          XPathExpressionException {
    return new HashMap<XPathExpression, Node>(0);
  }

  @Override
  protected String getRelPath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/beans.xml";
  }

}
