package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dom.client.StyleInjector;
import com.sun.tools.internal.ws.processor.generator.GeneratorException;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Finds all less files and compiles minifies and injects them.
 * @author edewit@redhat.com
 */
@CodeDecorator
@SuppressWarnings("UnusedDeclaration")
public class StyleGeneratorCodeDecorator extends IOCDecoratorExtension<Templated> {

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
        throw new GeneratorException("could not generate css from less file", e);
      }
    }

    final ContextualStatementBuilder inject = Stmt.create().invokeStatic(StyleInjector.class, "inject", sb.toString());
    return Lists.create(inject);
  }

  private String output(Class<?> baseClass, String sheet) throws IOException, UnableToCompleteException {
    final URL resource = baseClass.getResource("/" + sheet);
    final File cssFile = new LessConverter().convert(resource);

    return new StylesheetOptimizer(cssFile).output();
  }

  private String getCssResourceName(final String cssFilePath) {
    return cssFilePath;
  }
}
