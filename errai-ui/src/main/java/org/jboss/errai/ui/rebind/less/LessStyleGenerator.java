package org.jboss.errai.ui.rebind.less;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ui.client.local.spi.LessStyleMapping;
import org.jboss.errai.ui.rebind.TemplatedCodeDecorator;
import org.jboss.errai.ui.rebind.chain.TemplateChain;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dom.client.StyleInjector;

/**
 * This generator will create the LessStyleMapping that contains the mapping between the original selector name and
 * the obfuscated one. It will also make sure that the generated css file gets injected with a call
 * to {@link StyleInjector}.
 *
 * @author edewit@redhat.com
 */
@GenerateAsync(LessStyleMapping.class)
public class LessStyleGenerator extends AbstractAsyncGenerator {
  private static final String GENERATED_CLASS_NAME = "LessStyleMappingGenerated";

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(LessStyleMapping.class, context, logger,
            LessStyleMapping.class.getPackage().getName(), GENERATED_CLASS_NAME);
  }

  @Override
  protected String generate(TreeLogger logger, GeneratorContext context) {
    final ClassStructureBuilder<?> classBuilder = Implementations.extend(LessStyleMapping.class, GENERATED_CLASS_NAME);
    ConstructorBlockBuilder<?> constructor = classBuilder.publicConstructor();
    final Map<String, String> styleMapping = getStyleMapping();
    for (Map.Entry<String, String> entry : styleMapping.entrySet()) {
      constructor.append(Stmt.nestedCall(Refs.get("styleNameMapping")).invoke("put", entry.getKey(), entry.getValue()));
    }

    if (!styleMapping.isEmpty()) {
      final Collection<MetaClass> templated = ClassScanner.getTypesAnnotatedWith(Templated.class);

      for (MetaClass metaClass : templated) {
        String templateFileName = TemplatedCodeDecorator.getTemplateFileName(metaClass);

        final TemplateChain chain = TemplateChain.getInstance();
        chain.visitTemplate(templateFileName, metaClass);
      }
    }

    constructor.append(Stmt.create().invokeStatic(StyleInjector.class, "inject", createStyleSheet()));
    constructor.finish();

    return classBuilder.toJavaString();
  }

  public static Map<String, String> getStyleMapping() {
    Map<String, String> styleMapping = new HashMap<String, String>();
    for (StylesheetOptimizer stylesheet : LessStylesheetContext.getInstance().getOptimizedStylesheets()) {
      styleMapping.putAll(stylesheet.getConvertedSelectors());
    }
    return styleMapping;
  }

  private static String createStyleSheet() {
    StringBuilder sb = new StringBuilder();
    for (StylesheetOptimizer stylesheet : LessStylesheetContext.getInstance().getOptimizedStylesheets()) {
      sb.append(stylesheet.output());
    }
    return sb.toString();
  }
}
