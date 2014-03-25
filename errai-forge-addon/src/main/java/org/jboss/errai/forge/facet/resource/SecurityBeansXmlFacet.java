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
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SecurityBeansXmlFacet extends AbstractXmlResourceFacet {

  @Override
  protected Map<XPathExpression, Node> getRemovalMap(final XPath xPath, final Document doc)
          throws ParserConfigurationException,
          XPathExpressionException {
    return new HashMap<XPathExpression, Node>(0);
  }

  @Override
  protected Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException {
    final XPathExpression interceptorsPath = xPathFactory.newXPath().compile("/beans/interceptors");
    final Node interceptors = (Node) interceptorsPath.evaluate(doc, XPathConstants.NODE);
    if (interceptors == null) {
      doc.getFirstChild().insertBefore(doc.createElement("interceptors"), null);
    }

    final Map<XPathExpression, Collection<Node>> retVal = new HashMap<XPathExpression, Collection<Node>>();
    final Element userInterceptor = doc.createElement("class");
    userInterceptor.setTextContent("org.jboss.errai.security.server.SecurityUserInterceptor");
    final Element roleInterceptor = doc.createElement("class");
    roleInterceptor.setTextContent("org.jboss.errai.security.server.ServerSecurityRoleInterceptor");

    retVal.put(xPath.compile("/beans/interceptors"), Arrays.asList(new Node[] {
        userInterceptor,
        roleInterceptor
    }));

    return retVal;
  }

  @Override
  protected Map<XPathExpression, Node> getReplacements(final XPath xPath, final Document doc)
          throws ParserConfigurationException,
          XPathExpressionException {
    return new HashMap<XPathExpression, Node>(0);
  }

  @Override
  protected String getRelPath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/beans.xml";
  }

}
