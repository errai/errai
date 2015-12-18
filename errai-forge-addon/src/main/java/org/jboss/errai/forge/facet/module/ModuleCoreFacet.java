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

package org.jboss.errai.forge.facet.module;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.constant.ModuleVault.Module;
import org.jboss.errai.forge.xml.XmlParser;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * This facet creates a GWT module file if necessary and inherits the
 * {@literal com.google.gwt.user.User} module.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ProjectConfig.class })
public class ModuleCoreFacet extends AbstractModuleFacet {

  private Properties xmlProperties = new Properties();

  final static String emptyModuleContents =
          "<?xml version='1.0' encoding='UTF-8'?>\n"
                  + "<!DOCTYPE module PUBLIC '-//Google Inc.//DTD Google Web Toolkit 1.6//EN'\n\t"
                  + "'http://google-web-toolkit.googlecode.com/svn/releases/1.6/distro-source/core/src/gwt-module.dtd'>\n"
                  + "<module></module>\n";

  public ModuleCoreFacet() {
    modules = Arrays.asList(new Module[] {
        Module.GwtUser
    });
    xmlProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC, "-//Google Inc.//DTD Google Web Toolkit 1.6//EN");
    xmlProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
            "http://google-web-toolkit.googlecode.com/svn/releases/1.6/distro-source/core/src/gwt-module.dtd");
  }

  @Override
  public boolean install() {
    final File module = getProject().getFacet(ProjectConfig.class).getProjectProperty(ProjectProperty.MODULE_FILE,
            File.class);
    if (!module.exists()) {
      module.getParentFile().mkdirs();
      try {
        module.createNewFile();
      }
      catch (IOException e) {
        error("Could not create module at " + module.getAbsolutePath(), e);
        return false;
      }

      final FileWriter writer;
      try {
        writer = new FileWriter(module);
        writer.append(emptyModuleContents);
      }
      catch (IOException e) {
        error("Cannot write to module at " + module.getAbsolutePath(), e);
        return false;
      }

      try {
        if (writer != null)
          writer.close();
      }
      catch (IOException e) {
        warning("Could not close module file " + module.getAbsolutePath(), null);
      }

    }

    return super.install();
  }

  public String getModuleName() throws ParserConfigurationException, SAXException, IOException,
          TransformerConfigurationException, XPathExpressionException {
    final XmlParser xmlParser = xmlParserFactory.newXmlParser(getModuleFile(), xmlProperties);
    final XPath xPath = xPathFactory.newXPath();
    final Map<String, String> moduleAttributes = xmlParser.getAttributes(xPath.compile("/module"));

    String name = (moduleAttributes != null) ? moduleAttributes.get("rename-to") : null;
    if (name == null) {
      name = getProject().getFacet(ProjectConfig.class).getProjectProperty(ProjectProperty.MODULE_LOGICAL,
              String.class);
    }

    return name;
  }

  public void setModuleName(final String moduleName) throws SAXException, IOException, ParserConfigurationException,
          TransformerException, XPathExpressionException {
    final XmlParser xmlParser = xmlParserFactory.newXmlParser(getModuleFile(), xmlProperties);
    final XPath xPath = xPathFactory.newXPath();
    final Map<String, String> moduleAttributes = xmlParser.getAttributes(xPath.compile("/module"));

    if (moduleName != null && !moduleName.equals("")) {
      moduleAttributes.put("rename-to", moduleName);
    }
    else {
      moduleAttributes.remove("rename-to");
    }

    xmlParser.close();
  }

}
