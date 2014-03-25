package org.jboss.errai.forge.facet.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
  protected Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath, final Document doc) {
    return new HashMap<XPathExpression, Collection<Node>>(0);
  }

  @Override
  protected Map<XPathExpression, Node> getReplacements(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException {
    final XPathExpression key = xPath.compile(autoDiscoverParamExpression);

    final Element value = doc.createElement("init-param");
    value.appendChild(doc.createElement("param-name")).setTextContent("auto-discover-services");
    value.appendChild(doc.createElement("param-value")).setTextContent("false");

    final Map<XPathExpression, Node> replacements = new HashMap<XPathExpression, Node>(1);
    replacements.put(key, value);

    return replacements;
  }

  @Override
  protected String getRelPath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/web.xml";
  }

  @Override
  protected Map<XPathExpression, Node> getRemovalMap(XPath xPath, Document doc) throws ParserConfigurationException,
          XPathExpressionException {
    assert xPath.compile(erraiServletExpression).evaluate(erraiServletExpression, XPathConstants.NODE) != null;
    final XPathExpression key = xPath.compile(autoDiscoverParamExpression);

    final Element value = doc.createElement("init-param");
    value.appendChild(doc.createElement("param-name")).setTextContent("auto-discover-services");
    value.appendChild(doc.createElement("param-value")).setTextContent("true");

    final Map<XPathExpression, Node> replacements = new HashMap<XPathExpression, Node>(1);
    replacements.put(key, value);

    return replacements;
  }

}
