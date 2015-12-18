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
