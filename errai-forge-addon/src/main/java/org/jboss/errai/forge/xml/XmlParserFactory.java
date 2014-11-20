package org.jboss.errai.forge.xml;

import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

@ApplicationScoped
public class XmlParserFactory {

  private static final Properties DEFAULT_PROPERTIES = new Properties();

  static {
    DEFAULT_PROPERTIES.setProperty(OutputKeys.INDENT, "yes");
  }

  private final DocumentBuilderFactory documentBuilderFactory;
  private final TransformerFactory transformerFactory;
  
  public XmlParserFactory() {
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    transformerFactory = TransformerFactory.newInstance();
  }

  public XmlParser newXmlParser(final File xmlFile) throws TransformerConfigurationException,
          ParserConfigurationException, SAXException, IOException {
    return newXmlParser(xmlFile, DEFAULT_PROPERTIES);
  }

  public XmlParser newXmlParser(final File xmlFile, final Properties xmlProperties)
          throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {
    if (!xmlFile.exists()) {
      throw new IllegalStateException(String.format("The given xml file %s does not exist.", xmlFile.getAbsolutePath()));
    }

    final XmlParserImpl xmlParserImpl = new XmlParserImpl(xmlFile, xmlProperties,
            documentBuilderFactory.newDocumentBuilder(), transformerFactory.newTransformer());
    xmlParserImpl.open();

    return xmlParserImpl;
  }

}
