package org.jboss.errai.ui.rebind;

import org.apache.commons.io.IOUtils;
import org.cyberneko.html.parsers.DOMParser;
import org.jboss.errai.ui.shared.DomVisit;
import org.jboss.errai.ui.shared.DomVisitor;
import org.jboss.errai.ui.shared.TemplateVisitor;
import org.jboss.errai.ui.shared.chain.Chain;
import org.jboss.errai.ui.shared.chain.Command;
import org.jboss.errai.ui.shared.chain.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author edewit@redhat.com
 */
public class TemplateCatalog {
  public static final String ELEMENT = "CURRENT_ELEMENT";
  private static TemplateCatalog INSTANCE;
  private Map<URL, Context> contextMap = new HashMap<URL, Context>();
  private Chain chain = new Chain();

  protected TemplateCatalog() {}

  protected static TemplateCatalog createTemplateCatalog(Command... commands) {
    TemplateCatalog catalog = new TemplateCatalog();
    for (Command command : commands) {
      catalog.chain.addCommand(command);
    }
    return catalog;
  }

  public static TemplateCatalog getInstance() {
    if (INSTANCE == null) {
      INSTANCE = createTemplateCatalog(new TemplateVisitor());
    }

    return INSTANCE;
  }

  public void visitTemplate(URL template) {
    visitTemplate(template, new Context());
  }

  public void visitTemplate(URL template, Context context) {
    if (!contextMap.containsKey(template)) {
      final Document document = parseTemplate(template);
      visitTemplate(getRootNode(document), template, context);
    }
  }

  /**
   * Parses the template into a jtidy node.
   * @param template the location of the template to parse
   */
  private Document parseTemplate(URL template) {
    InputStream inputStream = null;
    try {
      inputStream = template.openStream();
      final DOMParser parser = new DOMParser();
      parser.parse(new InputSource(inputStream));
      return parser.getDocument();
    } catch (Exception e) {
      throw new IllegalArgumentException("could not read template " + template);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private Element getRootNode(Document document) {
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      return (Element) xpath.evaluate("//BODY", document, XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      throw new RuntimeException("could not find root node", e);
    }
  }

  private void visitTemplate(Element element, URL templateFileName, Context context) {
    contextMap.put(templateFileName, context);
    DomVisit.visit(element, new TemplateDomVisitor(templateFileName));
  }

  public Object getResult(URL template, String key) {
    final Context context = contextMap.get(template);
    if (context == null) {
      throw new IllegalArgumentException("no context found for template " + template);
    }
    return context.get(key);
  }

  /**
   * for testing purposes.
   * @return the initialized chain
   */
  Chain getChain() {
    return chain;
  }

  private class TemplateDomVisitor implements DomVisitor {

    private final URL templateFileName;

    private TemplateDomVisitor(URL templateFileName) {
      this.templateFileName = templateFileName;
    }

    @Override
    public boolean visit(Element element) {
      final Context context = contextMap.get(templateFileName);
      context.put(ELEMENT, element);
      chain.execute(context);
      return true;
    }
  }
}
