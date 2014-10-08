package org.jboss.errai.ui.rebind.less;

import java.util.Collection;
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
import org.jboss.errai.ui.rebind.chain.SelectorReplacer;
import org.jboss.errai.ui.rebind.chain.TemplateChain;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
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
  private static boolean needsToRun = false;

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(LessStyleMapping.class, context, logger,
            LessStyleMapping.class.getPackage().getName(), GENERATED_CLASS_NAME);
  }

  @Override
  protected String generate(TreeLogger logger, GeneratorContext context) {
    final ClassStructureBuilder<?> classBuilder = Implementations.extend(LessStyleMapping.class, GENERATED_CLASS_NAME);
    final ConstructorBlockBuilder<?> constructor = classBuilder.publicConstructor();
    final PropertyOracle oracle = context.getPropertyOracle();
    final LessStylesheetContext stylesheetContext = new LessStylesheetContext(logger, oracle);
    final Map<String, String> styleMapping = stylesheetContext.getStyleMapping();

    if (!styleMapping.isEmpty()) {
      for (Map.Entry<String, String> entry : styleMapping.entrySet()) {
        constructor.append(Stmt.nestedCall(Refs.get("styleNameMapping")).invoke("put", entry.getKey(), entry.getValue()));
      }
      
      final Collection<MetaClass> templated = ClassScanner.getTypesAnnotatedWith(Templated.class, context);
      for (MetaClass metaClass : templated) {
        String templateFileName = TemplatedCodeDecorator.getTemplateFileName(metaClass);
        final TemplateChain chain = new TemplateChain();
        chain.addCommand(new SelectorReplacer(styleMapping));
        chain.visitTemplate(templateFileName);
      }
      constructor.append(Stmt.create().invokeStatic(StyleInjector.class, "inject", stylesheetContext.getStylesheet()));

      // If this generator ran once it needs to rerun on every refresh (no
      // caching) because it moved the active template to a temporary location
      // which doesn't get updated otherwise.
      needsToRun = true;
    }

    constructor.finish();

    return classBuilder.toJavaString();
  }
  
  @Override
  protected boolean isCacheValid() {
    return super.isCacheValid() && !needsToRun;
  }
}
