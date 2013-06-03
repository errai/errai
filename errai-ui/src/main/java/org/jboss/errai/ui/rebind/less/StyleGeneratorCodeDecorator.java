package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dom.client.StyleInjector;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.rebind.TemplatedCodeDecorator;
import org.jboss.errai.ui.rebind.chain.SelectorReplacer;
import org.jboss.errai.ui.rebind.chain.TemplateChain;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.shared.chain.Context;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds all less files and compiles minifies and injects them.
 * @author edewit@redhat.com
 */
@CodeDecorator
@SuppressWarnings("UnusedDeclaration")
public class StyleGeneratorCodeDecorator extends IOCDecoratorExtension<Templated> {
  private Map<String, String> styleMapping = new HashMap<String, String>();

  public StyleGeneratorCodeDecorator(Class<Templated> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Templated> ctx) {
    StringBuilder sb = new StringBuilder();
    final Class<?> baseClass = ctx.getEnclosingType().asClass();
    final Collection<String> lessStyles = new LessStylesheetScanner(baseClass).getLessResources();
    for (String style : lessStyles) {
      try {
        sb.append(output(baseClass, style));
      } catch (Exception e) {
        throw new GenerationException("could not generate css from less file", e);
      }
    }

    final ContextualStatementBuilder inject = Stmt.create().invokeStatic(StyleInjector.class, "inject", sb.toString());
    String templateFileName = TemplatedCodeDecorator.getTemplateFileName(ctx.getElementType());
    Context context = new Context();
    context.put(SelectorReplacer.MAPPING, styleMapping);
    TemplateChain.getInstance().visitTemplate(getClass().getClassLoader().getResource(templateFileName), context);

    //TODO output the changed template...

    return Lists.create(inject);
  }

  private String output(Class<?> baseClass, String sheet) throws IOException, UnableToCompleteException {
    final URL resource = baseClass.getResource("/" + sheet);
    final File cssFile = new LessConverter().convert(resource);

    final StylesheetOptimizer stylesheetOptimizer = new StylesheetOptimizer(cssFile);
    styleMapping.putAll(stylesheetOptimizer.getConvertedSelectors());
    return stylesheetOptimizer.output();
  }

  private String getCssResourceName(final String cssFilePath) {
    return cssFilePath;
  }
}
