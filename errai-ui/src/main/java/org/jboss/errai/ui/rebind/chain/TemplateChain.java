package org.jboss.errai.ui.rebind.chain;

import static org.jboss.errai.ui.rebind.chain.TemplateCatalog.createTemplateCatalog;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.ui.shared.chain.Command;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.gwt.resources.ext.ResourceGeneratorUtil;

/**
 * @author edewit@redhat.com
 */
public class TemplateChain {
  final TemplateCatalog catalog = createTemplateCatalog(new DummyRemover());

  public void visitTemplate(String templateFileName) {
    URL template = getClass().getClassLoader().getResource(templateFileName);
    if (template == null) {
      throw new IllegalArgumentException("Could not find HTML template file: " + templateFileName);
    }

    final Document result = catalog.visitTemplate(template);
    writeDocumentToFile(result, templateFileName);
  }

  private void writeDocumentToFile(Document document, String templateFileName) {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer;
    try {
      transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "html");
      final Node root;
      if (isTemplateFragment(templateFileName)) {
        root = document.getElementsByTagName("body").item(0).getFirstChild();
      } else {
        root = document;
      }
      DOMSource source = new DOMSource(root);
      final String baseName = StringUtils.rightPad(FilenameUtils.getBaseName(templateFileName), 4, 'a');
      final File tempFile = File.createTempFile(baseName, ".html");
      StreamResult result = new StreamResult(tempFile);
      transformer.transform(source, result);

      //make sure GWT finds the altered template file instead of the original one
      ResourceGeneratorUtil.addNamedFile(templateFileName, tempFile);
    } catch (Exception e) {
      throw new GenerationException("could not write document to file", e);
    }
  }

  /*
   * There could be a better way to see if this template was a html fragment
   */
  private boolean isTemplateFragment(String templateFileName) {
    final String template;
    try {
      template = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(templateFileName));
    } catch (IOException e) {
      throw new GenerationException("could not read template file", e);
    }
    return !template.contains("body");
  }

  public void addCommand(Command command) {
    catalog.getChain().addCommand(command);
  }
}
