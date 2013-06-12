package org.jboss.errai.ui.rebind.chain;

import com.google.gwt.resources.ext.ResourceGeneratorUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.errai.codegen.exception.GenerationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.jboss.errai.ui.rebind.chain.TemplateCatalog.createTemplateCatalog;

/**
 * @author edewit@redhat.com
 */
public class TemplateChain {
  public static final String RESULT = "result";

  private static final TemplateChain INSTANCE = new TemplateChain();
  private static final TemplateCatalog catalog = createTemplateCatalog(new TranslateCommand(), new SelectorReplacer());

  private URL template;

  public static TemplateChain getInstance() {
    return INSTANCE;
  }

  public void visitTemplate(String templateFileName) {
    this.template = getClass().getClassLoader().getResource(templateFileName);
    catalog.visitTemplate(template);
    final Document result = getLastResult(RESULT);
    if (result != null) {
      writeDocumentToFile(result, templateFileName);
    }
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

  @SuppressWarnings("unchecked")
  public <T> T getLastResult(String key) {
    final Object result = catalog.getResult(template, key);
    return (T) result;
  }
}
