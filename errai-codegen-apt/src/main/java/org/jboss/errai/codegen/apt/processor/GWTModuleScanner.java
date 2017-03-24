/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.apt.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class GWTModuleScanner {

  public ScanResult scanFromRoot(final String moduleName) throws ParserConfigurationException, SAXException {

    final Set<String> processed = new HashSet<>();
    final Queue<String> todo = new LinkedList<>();
    todo.add(moduleName);
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    final SAXParser parser = factory.newSAXParser();
    final XMLReader reader = parser.getXMLReader();
    reader.setDTDHandler(new DummyHandler());
    reader.setEntityResolver(new DummyEntityResolver());
    reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    reader.setFeature("http://xml.org/sax/features/validation", false);

    final List<GWTModule> scanned = new ArrayList<>();
    final List<String> missing = new ArrayList<>();

    do {
      final String curModuleName = todo.poll();
      if (processed.add(curModuleName)) {
        final String path = convertToPath(curModuleName);
        final URL moduleXml = ClassLoader.getSystemResource(path);
        if (moduleXml != null) {
          try (InputStream is = moduleXml.openStream()) {
            final ModuleHandler moduleHandler = new ModuleHandler();
            reader.setContentHandler(moduleHandler);
            final InputSource inputSource = new InputSource(is);
            reader.parse(inputSource);
            final URL[] sourcePaths = convertSourcePathsToURLs(moduleXml, path, moduleHandler.getPaths());
            scanned.add(new GWTModule(moduleXml, sourcePaths, moduleHandler.getInherited().toArray(new String[0])));
            moduleHandler
              .getInherited()
              .stream()
              .forEach(todo::add);
          } catch (final IOException e) {
            missing.add(curModuleName);
          }
        }
        else {
          missing.add(curModuleName);
        }
      }
    } while (!todo.isEmpty());

    return new ScanResult(scanned, missing);
  }

  private URL[] convertSourcePathsToURLs(final URL moduleXml, final String moduleRelPath, final List<String> paths) {
    final String fullUrl = moduleXml.toExternalForm();
    final String moduleFile = new File(moduleRelPath).getName();
    final URI parent = URI.create(fullUrl.substring(0, fullUrl.indexOf(moduleFile)));

    return paths
            .stream()
            .map(path -> {
              try {
                return parent.resolve(path).toURL();
              } catch (final MalformedURLException e) {
                throw new RuntimeException(e);
              }
            })
            .toArray(URL[]::new);
  }

  private String convertToPath(final String moduleName) {
    return moduleName.replace('.', '/') + ".gwt.xml";
  }

  /**
   *
   * @author Max Barkley <mbarkley@redhat.com>
   */
  private final class DummyEntityResolver implements EntityResolver {
    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
      return null;
    }
  }

  /**
   *
   * @author Max Barkley <mbarkley@redhat.com>
   */
  private final class DummyHandler implements DTDHandler {
    @Override
    public void unparsedEntityDecl(final String name, final String publicId, final String systemId, final String notationName)
            throws SAXException {
    }

    @Override
    public void notationDecl(final String name, final String publicId, final String systemId) throws SAXException {
    }
  }

  private static class ModuleHandler extends DefaultHandler {
    private final List<String> inherited = new ArrayList<>();
    private final List<String> paths = new ArrayList<>();
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
      switch (qName) {
      case "inherits": {
        final String value = attributes.getValue("name");
        if (value != null) {
          inherited.add(value);
        }
      }
      case "source": {
        final String value = attributes.getValue("path");
        if (value != null) {
          paths.add(value);
        }
      }
      }
    }
    public List<String> getInherited() {
      return inherited;
    }
    public List<String> getPaths() {
      if (paths.isEmpty()) {
        return Collections.singletonList("client");
      }
      else {
        return paths;
      }
    }
  }

  public static class ScanResult {
    private final List<GWTModule> scanned;
    private final List<String> missing;

    public ScanResult(final List<GWTModule> scanned, final List<String> missing) {
      this.scanned = scanned;
      this.missing = missing;
    }
    public List<GWTModule> getScanned() {
      return scanned;
    }
    public List<String> getMissing() {
      return missing;
    }

    @Override
    public int hashCode() {
      return Objects.hash(scanned, missing);
    }

    @Override
    public boolean equals(final Object obj) {
      return obj instanceof ScanResult && Objects.equals(scanned, ((ScanResult) obj).getScanned())
              && Objects.equals(missing, ((ScanResult) obj).getMissing());
    }

    @Override
    public String toString() {
      return String.format("ScanResult{scanned=%s, missing=%s}", scanned, missing);
    }
  }
}
