/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.DomVisit;
import org.jboss.errai.ui.shared.chain.Chain;
import org.jboss.errai.ui.shared.chain.Command;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author edewit@redhat.com
 */
public class TemplateCatalog {
  final private Chain chain = new Chain();

  public static TemplateCatalog createTemplateCatalog(Command... commands) {
    TemplateCatalog catalog = new TemplateCatalog();
    for (Command command : commands) {
      catalog.chain.addCommand(command);
    }
    return catalog;
  }

  public Document visitTemplate(URL template) {
    final Document document = parseTemplate(template);
    for (int i = 0; i < document.getChildNodes().getLength(); i++) {
      final Node node = document.getChildNodes().item(i);
      if (node instanceof Element) {
        DomVisit.visit((Element) node, element -> {
          chain.execute(element);
          return true;
        });
      }
    }
    return document;
  }

  /**
   * Parses the template into a document.
   *
   * @param template the location of the template to parse
   */
  public Document parseTemplate(URL template) {
    try (InputStream  inputStream = template.openStream()){
      return jsoup2DOM(Jsoup.parse(inputStream, "UTF-8", ""));
    } catch (Exception e) {
       throw new IllegalArgumentException("could not read template " + template, e);
    }
  }

  /**
   * for testing purposes.
   *
   * @return the initialized chain
   */
  Chain getChain() {
    return chain;
  }

    /**
     * JSoup to Dom converter. Adopted from <a href="https://github.com/apache/stanbol">Apache Stanbol</a>.
     * @see <a href="https://github.com/apache/stanbol/blob/d1500ffba507dce0e43f228342aad97cae7cb0e3/enhancement-engines/htmlextractor/src/main/java/org/apache/stanbol/enhancer/engines/htmlextractor/impl/DOMBuilder.java">DOMBuilder</a>
     *
     * @param jsoupDocument JSoup dom tree
     * @return xml dom tree
     */
  private static Document jsoup2DOM(org.jsoup.nodes.Document jsoupDocument) {

    try {

      /* Obtain the document builder for the configured XML parser. */
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      /* Create a document to contain the content. */
      Document document = docBuilder.newDocument();
      createDOM(jsoupDocument, document, document, new HashMap<>());
      return document;
    } catch (ParserConfigurationException pce) {
        throw new RuntimeException(pce);
    }


  }

  /**
   * The internal helper that copies content from the specified Jsoup <tt>Node</tt> into a W3C {@link Node}.
   * Adopted from <a href="https://github.com/apache/stanbol">Apache Stanbol</a>.
   *
   * @see <a href="https://github.com/apache/stanbol/blob/d1500ffba507dce0e43f228342aad97cae7cb0e3/enhancement-engines/htmlextractor/src/main/java/org/apache/stanbol/enhancer/engines/htmlextractor/impl/DOMBuilder.java">DOMBuilder</a>
   * @param node The Jsoup node containing the content to copy to the specified W3C {@link Node}.
   * @param out The W3C {@link Node} that receives the DOM content.
   */
  private static void createDOM(org.jsoup.nodes.Node node, Node out, Document doc, Map<String,String> ns) {

    if (node instanceof org.jsoup.nodes.Document) {

      org.jsoup.nodes.Document d = ((org.jsoup.nodes.Document) node);
      for (org.jsoup.nodes.Node n : d.childNodes()) {
        createDOM(n, out,doc,ns);
      }

    } else if (node instanceof org.jsoup.nodes.Element) {

      org.jsoup.nodes.Element jsoupElement = ((org.jsoup.nodes.Element) node);
      org.w3c.dom.Element domElement = doc.createElement(jsoupElement.tagName());
      out.appendChild(domElement);
      org.jsoup.nodes.Attributes attributes = jsoupElement.attributes();

      for(org.jsoup.nodes.Attribute a : attributes){
        String attName = a.getKey();
        //omit xhtml namespace
        if (attName.equals("xmlns")) {
          continue;
        }
        String attPrefix = getNSPrefix(attName);
        if (attPrefix != null) {
          if (attPrefix.equals("xmlns")) {
            ns.put(getLocalName(attName), a.getValue());
          }
          else if (!attPrefix.equals("xml")) {
            String namespace = ns.get(attPrefix);
            if (namespace == null) {
              //fix attribute names looking like qnames
              attName = attName.replace(':','_');
            }
          }
        }
        domElement.setAttribute(attName, a.getValue());
      }

      for (org.jsoup.nodes.Node n : jsoupElement.childNodes()) {
        createDOM(n, domElement, doc,ns);
      }

    } else if (node instanceof org.jsoup.nodes.TextNode) {

      org.jsoup.nodes.TextNode t = ((org.jsoup.nodes.TextNode) node);
      if (!(out instanceof Document)) {
        out.appendChild(doc.createTextNode(t.text()));
      }
    }
  }

  // some hacks for handling namespace in jsoup2DOM conversion
  private static String getNSPrefix(String name) {
    if (name != null) {
      int pos = name.indexOf(':');
      if (pos > 0) {
        return name.substring(0,pos);
      }
    }
    return null;
  }

  private static String getLocalName(String name) {
    if (name != null) {
      int pos = name.lastIndexOf(':');
      if (pos > 0) {
        return name.substring(pos+1);
      }
    }
    return name;
  }

}