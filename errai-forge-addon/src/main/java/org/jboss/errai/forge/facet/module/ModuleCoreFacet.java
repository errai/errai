package org.jboss.errai.forge.facet.module;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.plexus.util.cli.shell.Shell;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectConfig.ProjectProperty;
import org.jboss.errai.forge.constant.ModuleVault.Module;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This facet creates a GWT module file if necessary and inherits the
 * {@literal com.google.gwt.user.User} module.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ProjectConfig.class })
public class ModuleCoreFacet extends AbstractModuleFacet {

  private final TransformerFactory transFactory = TransformerFactory.newInstance();

  final static String emptyModuleContents =
          "<?xml version='1.0' encoding='UTF-8'?>\n"
                  + "<!DOCTYPE module PUBLIC '-//Google Inc.//DTD Google Web Toolkit 1.6//EN'\n\t"
                  + "'http://google-web-toolkit.googlecode.com/svn/releases/1.6/distro-source/core/src/gwt-module.dtd'>\n"
                  + "<module></module>\n";

  public ModuleCoreFacet(final Shell shell) {
    this();
    this.shell = shell;
  }

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

  public String getModuleName() throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    final Document doc = docBuilder.parse(getModuleFile());

    final NodeList moduleTags = doc.getElementsByTagName("module");
    if (moduleTags.getLength() != 1) {
      throw new IllegalStateException(String.format("The gwt module, %s, is malformed.", getModuleFile()));
    }

    String name = ((Element) moduleTags.item(0)).getAttribute("rename-to");
    if (name.equals("")) {
      name = getProject().getFacet(ProjectConfig.class).getProjectProperty(ProjectProperty.MODULE_LOGICAL,
              String.class);
    }

    return name;
  }

  public void setModuleName(String moduleName) throws SAXException, IOException, ParserConfigurationException,
          TransformerException {
    final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    final Document doc = docBuilder.parse(getModuleFile());

    final NodeList moduleTags = doc.getElementsByTagName("module");
    if (moduleTags.getLength() != 1) {
      throw new RuntimeException(String.format("The GWT module %s is malformed: %d <module> tags found.",
              getModuleFile(), moduleTags.getLength()));

    }

    if (moduleName != null && !moduleName.equals("")) {
      ((Element) moduleTags.item(0)).setAttribute("rename-to", moduleName);
    }
    else {
      ((Element) moduleTags.item(0)).removeAttribute("rename-to");
    }

    final Transformer transformer = transFactory.newTransformer();
    final DOMSource source = new DOMSource(doc);
    final StreamResult streamResult = new StreamResult(getModuleFile());

    transformer.setOutputProperties(xmlProperties);
    transformer.transform(source, streamResult);
  }

}
