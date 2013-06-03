package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.TemplateVisitor;
import org.jboss.errai.ui.shared.chain.Command;
import org.jboss.errai.ui.shared.chain.Context;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.*;

/**
 * Command version of the TemplateVisitor this command executes in a chain of commands for each element in the DOM tree.
 * @author edewit@redhat.com
 */
public class TranslateCommand extends TemplateVisitor implements Command {

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

  public static Context buildContext(String templateFragment, String i18nPrefix) {
    final Context context = new Context();
    context.put(PREFIX, i18nPrefix);
    if (templateFragment != null && templateFragment.trim().length() > 0) {
      context.put(FRAGMENT, templateFragment);
    }
    return context;
  }

  @Override
  public void execute(Context context) {
    Node parent = (Node) context.get(DONE);
    Element element = (Element) context.get(TemplateCatalog.ELEMENT);
    if (parent != null) {
      if (element.getParentNode().isEqualNode(parent)) {
        return;
      } else {
        context.remove(DONE);
      }
    }
    setI18nPrefix((String) context.get(PREFIX));
    context.put(VALUES, getI18nValues());
    templateFragment = (String) context.get(FRAGMENT);
    if (!visit(element)) {
      context.put(DONE, element);
    }
  }
}
