package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dom.client.StyleInjector;
import org.apache.commons.io.IOUtils;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ui.client.local.spi.LessStyleMapping;
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
@GenerateAsync(LessStyleMapping.class)
public class LessStyleGenerator extends AbstractAsyncGenerator {
  private static final String GENERATED_CLASS_NAME = "LessStyleMappingGenerated";
  private static Set<StylesheetOptimizer> optimizedStylesheets = new HashSet<StylesheetOptimizer>();

  static {
    try {
      init();
    } catch (Exception e) {
      throw new GenerationException("could not generate css from less file", e);
    }
  }

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(LessStyleMapping.class, context, logger,
            LessStyleMapping.class.getPackage().getName(), GENERATED_CLASS_NAME);
  }

  @Override
  protected String generate(TreeLogger logger, GeneratorContext context) {
    final ClassStructureBuilder<?> classBuilder = Implementations.extend(LessStyleMapping.class, GENERATED_CLASS_NAME);
    ConstructorBlockBuilder<?> constructor = classBuilder.publicConstructor();
    for (Map.Entry<String, String> entry : getStyleMapping().entrySet()) {
      constructor.append(Stmt.nestedCall(Refs.get("styleNameMapping")).invoke("put", entry.getKey(), entry.getValue()));
    }

    final Collection<MetaClass> templated = ClassScanner.getTypesAnnotatedWith(Templated.class);

    for (MetaClass metaClass : templated) {
      String templateFileName = TemplatedCodeDecorator.getTemplateFileName(metaClass);
      final TemplateChain chain = TemplateChain.getInstance();
      chain.visitTemplate(getClass().getClassLoader().getResource(templateFileName));

      final Document document = chain.getLastResult(RESULT);
      if (!EnvUtil.isJUnitTest()) {
        writeDocumentToFile(document, templateFileName);
      }
    }

    constructor.append(Stmt.create().invokeStatic(StyleInjector.class, "inject", createStyleSheet()));
    constructor.finish();

    return classBuilder.toJavaString();
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
      final URL resource = LessStyleGenerator.class.getResource("/" + sheet);
      final File cssFile = new LessConverter().convert(resource);

      final StylesheetOptimizer stylesheetOptimizer = new StylesheetOptimizer(cssFile);
      optimizedStylesheets.add(stylesheetOptimizer);
    }
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
