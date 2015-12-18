package org.jboss.errai.ui.rebind.less;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ui.client.local.spi.LessStyleMapping;
import org.jboss.errai.ui.rebind.TemplatedCodeDecorator;
import org.jboss.errai.ui.rebind.chain.SelectorReplacer;
import org.jboss.errai.ui.rebind.chain.TemplateChain;
import org.jboss.errai.ui.shared.api.annotations.StyleDescriptor;
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

    final Collection<URL> stylesheets = getStylesheets(context);
    if (!stylesheets.isEmpty()) {
      final LessStylesheetContext stylesheetContext = new LessStylesheetContext(logger, context.getPropertyOracle());
      final Collection<MetaClass> templated = ClassScanner.getTypesAnnotatedWith(Templated.class, context);

      try {
        stylesheetContext.compileLessStylesheets(stylesheets);
      } catch (IOException e) {
        throw new GenerationException(e);
      }

      addStyleMappingsToConstructor(constructor, stylesheetContext);
      performCssTransformations(stylesheetContext, templated);
      addStyleInjectorCallToConstructor(constructor, stylesheetContext);

      // If this generator ran once it needs to rerun on every refresh (no
      // caching) because it moved the active template to a temporary location
      // which doesn't get updated otherwise.
      needsToRun = true;
    }

    constructor.finish();
    return classBuilder.toJavaString();
  }

  private void performCssTransformations(final LessStylesheetContext stylesheetContext, final Collection<MetaClass> templated) {
    for (MetaClass metaClass : templated) {
      final String templateFileName = TemplatedCodeDecorator.getTemplateFileName(metaClass);

      final TemplateChain chain = new TemplateChain();
      chain.addCommand(new SelectorReplacer(stylesheetContext.getStyleMapping()));
      chain.visitTemplate(templateFileName);
    }
  }

  private void addStyleInjectorCallToConstructor(ConstructorBlockBuilder<?> constructor, final LessStylesheetContext stylesheetContext) {
    constructor.append(Stmt.create().invokeStatic(StyleInjector.class, "inject", stylesheetContext.getStylesheet()));
  }

  private void addStyleMappingsToConstructor(ConstructorBlockBuilder<?> constructor, final LessStylesheetContext stylesheetContext) {
    for (Map.Entry<String, String> entry : stylesheetContext.getStyleMapping().entrySet()) {
      constructor.append(Stmt.nestedCall(Refs.get("styleNameMapping")).invoke("put", entry.getKey(), entry.getValue()));
    }
  }

  private Collection<URL> getStylesheets(final GeneratorContext context) {
    final Class<?> styleDescriptorClass = getStyleDescriptorClass(context);
    final Collection<URL> stylesheets = new ArrayList<URL>();

    if (styleDescriptorClass != null) {
      final StyleDescriptor styleDescriptor = styleDescriptorClass.getAnnotation(StyleDescriptor.class);
      for (final String path : styleDescriptor.value()) {
        stylesheets.add(transformPath(styleDescriptorClass, path));
      }
    }

    return stylesheets;
  }

  private Class<?> getStyleDescriptorClass(final GeneratorContext context) {
    final Collection<MetaClass> foundTypes = ClassScanner.getTypesAnnotatedWith(StyleDescriptor.class, context);
    if (foundTypes.size() > 1) {
      throw new GenerationException("Found multiple types annotated with @StyleDescriptor (There should only be one): "
              + foundTypes);
    }
    else if (foundTypes.size() == 1) {
      return foundTypes.iterator().next().asClass();
    }
    else {
      return null;
    }
  }

  private URL transformPath(final Class<?> descriptorClass, final String stylesheet) {
    final URL resource = descriptorClass.getResource(stylesheet);
    if (resource == null)
      throw new GenerationException("Could not find stylesheet " + stylesheet + " declared in @Templated class " + descriptorClass);

    return resource;
  }

  @Override
  protected boolean isCacheValid() {
    return !needsToRun && super.isCacheValid();
  }
}
