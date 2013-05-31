package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.TemplateVisitor;
import org.jboss.errai.ui.shared.chain.Command;
import org.jboss.errai.ui.shared.chain.Context;
import org.w3c.dom.Element;

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
  }

  private String templateFragment;
  private Boolean foundTemplateFragment;

  public TranslateCommand() {
    super("");
  }

  @Override
  public boolean visit(Element element) {
    if (templateFragment != null && !foundTemplateFragment && templateFragment.equals(element.getAttribute("data-field"))) {
      foundTemplateFragment = true;
      return super.visit(element);
    }
    return true;
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
    Element element = (Element) context.get(TemplateCatalog.ELEMENT);
    setI18nPrefix((String) context.get(PREFIX));
    context.put(VALUES, getI18nValues());
    templateFragment = (String) context.get(FRAGMENT);
    visit(element);
  }
}
