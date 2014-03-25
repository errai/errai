package org.jboss.errai.forge.facet.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This facet configures the ErraiServlet used by the errai-bus project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ WebXmlFacet.class })
public class ErraiBusServletConfigFacet extends AbstractXmlResourceFacet {

  public static final String webXmlRootExpression = "/web-app";

  @Override
  protected Map<XPathExpression, Collection<Node>> getElementsToInsert(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException {
    final Element servlet = doc.createElement("servlet");
    servlet.appendChild(doc.createElement("servlet-name")).setTextContent("ErraiServlet");
    servlet.appendChild(doc.createElement("servlet-class")).setTextContent(
            "org.jboss.errai.bus.server.servlet.DefaultBlockingServlet");

    final Node initParam = servlet.appendChild(doc.createElement("init-param"));
    initParam.appendChild(doc.createElement("param-name")).setTextContent("auto-discover-services");
    initParam.appendChild(doc.createElement("param-value")).setTextContent("true");

    servlet.appendChild(doc.createElement("load-on-startup")).setTextContent("1");

    final Element servletMapping = doc.createElement("servlet-mapping");
    servletMapping.appendChild(doc.createElement("servlet-name")).setTextContent("ErraiServlet");
    servletMapping.appendChild(doc.createElement("url-pattern")).setTextContent("*.erraiBus");

    final Map<XPathExpression, Collection<Node>> retVal = new HashMap<XPathExpression, Collection<Node>>(1);
    final Collection<Node> nodes = new ArrayList<Node>(2);

    nodes.add(servlet);
    nodes.add(servletMapping);
    retVal.put(xPath.compile(webXmlRootExpression), nodes);

    return retVal;
  }

  @Override
  protected Map<XPathExpression, Collection<Node>> getElementsToVerify(final XPath xPath, final Document doc)
          throws ParserConfigurationException, XPathExpressionException {
    final Map<XPathExpression, Collection<Node>> retVal = getElementsToInsert(xPath, doc);

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
  protected Map<XPathExpression, Node> getReplacements(final XPath xPath, final Document doc) {
    return new HashMap<XPathExpression, Node>(0);
  }

  @Override
  protected String getRelPath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/web.xml";
  }

  @Override
  protected Map<XPathExpression, Node> getRemovalMap(XPath xPath, Document doc) throws ParserConfigurationException,
          XPathExpressionException {
    return new HashMap<XPathExpression, Node>(0);
  }

}
