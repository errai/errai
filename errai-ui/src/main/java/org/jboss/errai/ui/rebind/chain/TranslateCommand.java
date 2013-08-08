package org.jboss.errai.ui.rebind.chain;

import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.DONE;
import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.FRAGMENT;
import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.PREFIX;
import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.VALUES;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Command version of the TemplateVisitor this command executes in a chain of commands for each element in the DOM tree.
 * @author edewit@redhat.com
 */
public class TranslateCommand extends TemplateVisitor implements Command {
  protected Map<MetaClass, Context> contexts = new HashMap<MetaClass, Context>();

  public class Constants {
    public static final String PREFIX ="i18nPrefix";
    public static final String VALUES = "i18nValues";
    public static final String FRAGMENT = "templateFragment";
    public static final String DONE = "done";
  }

  private Element fragmentRoot;
  
  private String templateFragment;

  public TranslateCommand() {
    super("");
  }

  @Override
  public boolean visit(Element element) {
    if (templateFragment == null) {
      return super.visit(element);
    }
    
    // if we've found the fragment root already, check if we're still under it 
    if (fragmentRoot != null) {
      if ( (fragmentRoot.compareDocumentPosition(element) & Element.DOCUMENT_POSITION_CONTAINED_BY) != 0) {
        // still under it
        return super.visit(element);
      }
      else {
        // went past it
        return true;
      }
    }

    // check if this is the fragment root
    if (templateFragment.equals(element.getAttribute("data-field"))) {
      fragmentRoot = element;
      return super.visit(element);
    }
    
    // haven't reached fragment root yet
    return true;
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

      contexts.put(templatedAnnotatedClass, subContext);
    }

    return new Context();
  }

  @Override
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

    final MetaClass template = (MetaClass) context.get(TemplateCatalog.FILENAME);
    Context subContext = contexts.get(template);
    context.putAll(subContext);

    setI18nPrefix((String) context.get(PREFIX));
    context.put(VALUES, getI18nValues());
    String newTemplateFragment = (String) context.get(FRAGMENT);
    if (areDifferent(templateFragment, newTemplateFragment)) {
      fragmentRoot = null;
    }
    templateFragment = newTemplateFragment;
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
  
  private static boolean areDifferent(String s1, String s2) {
    if (s1 == s2) return false;
    if (s1 != null && s1.equals(s2)) return false;
    return true;
  }
}
