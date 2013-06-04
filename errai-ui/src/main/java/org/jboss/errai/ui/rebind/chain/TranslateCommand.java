package org.jboss.errai.ui.rebind.chain;

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

import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.*;

/**
 * Command version of the TemplateVisitor this command executes in a chain of commands for each element in the DOM tree.
 * @author edewit@redhat.com
 */
public class TranslateCommand extends TemplateVisitor implements Command {
  protected Map<URL, Context> contexts = new HashMap<URL, Context>();

  public class Constants {
    public static final String PREFIX ="i18nPrefix";
    public static final String VALUES = "i18nValues";
    public static final String FRAGMENT = "templateFragment";
    public static final String DONE = "done";
  }

  private String templateFragment;
  private boolean foundTemplateFragment;

  public TranslateCommand() {
    super("");
  }

  @Override
  public boolean visit(Element element) {
    if (templateFragment != null && !foundTemplateFragment) {
      foundTemplateFragment = templateFragment.equals(element.getAttribute("data-field"));
      return true;
    }
    if (foundTemplateFragment) {
      return super.visit(element);
    } else {
      return super.visit(element);
    }
  }

  @Override
  public Context createInitialContext() {
    final Collection<MetaClass> templatedAnnotatedClasses = ClassScanner.getTypesAnnotatedWith(Templated.class);
    for (MetaClass templatedAnnotatedClass : templatedAnnotatedClasses) {
      String templateFileName = TemplatedCodeDecorator.getTemplateFileName(templatedAnnotatedClass);
      String templateFragment = TemplatedCodeDecorator.getTemplateFragmentName(templatedAnnotatedClass);
      String i18nPrefix = TemplateUtil.getI18nPrefix(templateFileName);

      Context subContext = new Context();
      subContext.put(PREFIX, i18nPrefix);
      if (templateFragment != null && templateFragment.trim().length() > 0) {
        subContext.put(FRAGMENT, templateFragment);
      }

      final URL resource = getClass().getClassLoader().getResource(templateFileName);
      contexts.put(resource, subContext);
    }

    return new Context();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(Context context) {
    Node parent = (Node) context.get(DONE);
    Element element = (Element) context.get(TemplateCatalog.ELEMENT);
    if (parent != null) {
      if (isElementParentOf(parent, element)) {
        return;
      } else {
        context.remove(DONE);
      }
    }

    final URL fileName = (URL) context.get(TemplateCatalog.FILENAME);
    Context subContext = contexts.get(fileName);
    context.putAll(subContext);

    setI18nPrefix((String) context.get(PREFIX));
    context.put(VALUES, getI18nValues());
    templateFragment = (String) context.get(FRAGMENT);
    if (!visit(element)) {
      context.put(DONE, element);
    }
  }

  private boolean isElementParentOf(Node parent, Element element) {
    Node elementParent = element.getParentNode();
    while (elementParent != null) {
      if (elementParent.isEqualNode(parent)) {
        return true;
      }
      elementParent = elementParent.getParentNode();
    }

    return false;
  }
}
