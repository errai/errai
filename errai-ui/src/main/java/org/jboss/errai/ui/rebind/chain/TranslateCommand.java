package org.jboss.errai.ui.rebind.chain;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ui.rebind.TemplatedCodeDecorator;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.jboss.errai.ui.shared.TemplateVisitor;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.shared.chain.Command;
import org.jboss.errai.ui.shared.chain.Context;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.*;

/**
 * Command version of the TemplateVisitor this command executes in a chain of commands for each element in the DOM tree.
 * @author edewit@redhat.com
 */
public class TranslateCommand implements Command {
  protected Multimap<URL, TemplateVisitor> contexts = ArrayListMultimap.create();

  public class Constants {
    public static final String VALUES = "i18nValues";
  }

  @Override
  public Context createInitialContext() {
    final Collection<MetaClass> templatedAnnotatedClasses = ClassScanner.getTypesAnnotatedWith(Templated.class);
    for (MetaClass templatedAnnotatedClass : templatedAnnotatedClasses) {
      String templateFileName = TemplatedCodeDecorator.getTemplateFileName(templatedAnnotatedClass);
      String i18nPrefix = TemplateUtil.getI18nPrefix(templateFileName);

      final URL resource = getClass().getClassLoader().getResource(templateFileName);
      contexts.put(resource, new TemplateVisitor(i18nPrefix));
    }

    return new Context();
  }

  @Override
  public void execute(Context context) {
    final URL fileName = (URL) context.get(TemplateCatalog.FILENAME);
    Collection<TemplateVisitor> visitors = contexts.get(fileName);

    for (TemplateVisitor templateVisitor : visitors) {
      templateVisitor.visit((Element) context.get(TemplateCatalog.ELEMENT));
      context.put(VALUES, templateVisitor.getI18nValues());
    }
  }
}
