package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dom.client.StyleInjector;
import org.apache.commons.io.IOUtils;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.rebind.TemplatedCodeDecorator;
import org.jboss.errai.ui.rebind.chain.TemplateChain;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static org.jboss.errai.ui.rebind.chain.SelectorReplacer.RESULT;

/**
 * Finds all less files and compiles minifies and injects them.
 *
 * @author edewit@redhat.com
 */
@CodeDecorator
@SuppressWarnings("UnusedDeclaration")
public class StyleGeneratorCodeDecorator extends IOCDecoratorExtension<Templated> {
  private static Set<StylesheetOptimizer> optimizedStylesheets = new HashSet<StylesheetOptimizer>();

  static {
    try {
      init();
    } catch (Exception e) {
      throw new GenerationException("could not generate css from less file", e);
    }
  }

  public StyleGeneratorCodeDecorator(Class<Templated> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Templated> ctx) {
    final ContextualStatementBuilder inject = Stmt.create().invokeStatic(StyleInjector.class, "inject", createStyleSheet());
    String templateFileName = TemplatedCodeDecorator.getTemplateFileName(ctx.getElementType());
    final TemplateChain chain = TemplateChain.getInstance();
    chain.visitTemplate(getClass().getClassLoader().getResource(templateFileName));

    final Document document = chain.getLastResult(RESULT);
    if (!EnvUtil.isJUnitTest()) {
      writeDocumentToFile(document, templateFileName);
    }

    return Lists.create(inject);
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
      final URI uri = getClass().getClassLoader().getResource(templateFileName).toURI();
      StreamResult result = new StreamResult(new File(uri));
      transformer.transform(source, result);
    } catch (Exception e) {
      throw new GenerationException("could not write document to file", e);
    }
  }

  private boolean isTemplateFragment(String templateFileName) throws IOException {
    final String template = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(templateFileName));
    return !template.contains("body");
  }

  private static void init() throws IOException, UnableToCompleteException {
    final Collection<String> lessStyles = new LessStylesheetScanner().getLessResources();
    for (String sheet : lessStyles) {
      final URL resource = StyleGeneratorCodeDecorator.class.getResource("/" + sheet);
      final File cssFile = new LessConverter().convert(resource);

      final StylesheetOptimizer stylesheetOptimizer = new StylesheetOptimizer(cssFile);
      optimizedStylesheets.add(stylesheetOptimizer);
    }
  }

  private String getCssResourceName(final String cssFilePath) {
    return cssFilePath;
  }

  public static Map<String, String> getStyleMapping() {
    Map<String, String> styleMapping = new HashMap<String, String>();
    for (StylesheetOptimizer stylesheet : optimizedStylesheets) {
      styleMapping.putAll(stylesheet.getConvertedSelectors());
    }
    return styleMapping;
  }

  private static String createStyleSheet() {
    StringBuilder sb = new StringBuilder();
    for (StylesheetOptimizer stylesheet : optimizedStylesheets) {
      sb.append(stylesheet.output());
    }
    return sb.toString();
  }
}
