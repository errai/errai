package org.jboss.errai.ui.shared;

import java.util.Stack;

import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.w3c.dom.Element;

/**
 * Visit DOM elements and translate sub-trees which are Errai {@link Templated}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class TranslationDomRevisitor implements DomRevisitor {
  /*
   * Outline:
   * 
   * Root nodes of templates are marked with 'data-i18n-prefix' attribute, allowing translation
   * values to be looked up. Use a stack to keep track of which template we are in. After visiting,
   * if the top of the stack matches an element prefix attribute, we are leaving that template.
   */

  private TemplateTranslationVisitor visitor = new TemplateTranslationVisitor("");
  private final Stack<String> prefixes = new Stack<String>();
  private static final String PREFIX = "data-i18n-prefix";

  @Override
  public boolean visit(Element element) {
    if (visitor.hasAttribute(element, PREFIX))
      prefixes.push(element.getAttribute(PREFIX));

    if (prefixes.empty())
      return !visitor.isTextOnly(element);

    visitor.setI18nPrefix(prefixes.peek());
    return visitor.visit(element);
  }

  @Override
  public void afterVisit(Element element) {
    if (visitor.hasAttribute(element, PREFIX) && element.getAttribute(PREFIX).equals(prefixes.peek()))
      prefixes.pop();
  }
}
